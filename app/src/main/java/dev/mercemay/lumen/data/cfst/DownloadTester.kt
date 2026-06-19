package dev.mercemay.lumen.data.cfst

import android.net.Network
import dev.mercemay.lumen.data.network.OkHttpClientFactory
import dev.mercemay.lumen.domain.model.PingResult
import dev.mercemay.lumen.domain.model.SpeedTestConfig
import dev.mercemay.lumen.domain.model.SpeedTestResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import okhttp3.Request
import java.net.URI
import kotlin.coroutines.coroutineContext
import kotlin.math.max

class DownloadTester(
    private val okHttpClientFactory: OkHttpClientFactory,
    private val coloParser: ColoParser = ColoParser(),
) {
    suspend fun test(
        candidates: List<PingResult>,
        config: SpeedTestConfig,
        network: Network?,
        onProgress: (completed: Int, total: Int, currentIp: String?) -> Unit = { _, _, _ -> },
    ): List<SpeedTestResult> = withContext(Dispatchers.IO) {
        if (config.disableDownload) {
            return@withContext candidates.take(config.downloadTestCount).map { ping ->
                SpeedTestResult(
                    candidate = ping.candidate,
                    port = ping.port,
                    sent = ping.sent,
                    received = ping.received,
                    lossRate = ping.lossRate,
                    averageDelayMillis = ping.averageDelayMillis,
                    downloadSpeedBytesPerSecond = 0.0,
                    colo = ping.colo,
                )
            }
        }

        val targetCount = config.downloadTestCount.coerceAtLeast(1)
        val results = ArrayList<SpeedTestResult>()
        for (ping in candidates) {
            coroutineContext.ensureActive()
            onProgress(results.size, targetCount, ping.candidate.hostAddress)
            val result = runCatching { testOne(ping, config, network) }.onFailure { e ->
                android.util.Log.e("DownloadTester", "Failed ${ping.candidate.hostAddress}:${ping.port}", e)
            }.getOrNull()
            if (result != null && result.downloadSpeedMbps >= config.minDownloadSpeedMbps) {
                results += result
            }
            if (results.size >= targetCount) break
        }
        onProgress(results.size, targetCount, null)
        results.sortedByDescending { it.downloadSpeedBytesPerSecond }
    }

    private suspend fun testOne(
        ping: PingResult,
        config: SpeedTestConfig,
        network: Network?,
    ): SpeedTestResult = withContext(Dispatchers.IO) {
        val targetUrl = withPort(config.testUrl, ping.port)
        val client = okHttpClientFactory.forCandidate(
            candidate = ping.candidate.address,
            network = network,
            timeoutSeconds = config.downloadTimeoutSeconds.toLong().coerceAtLeast(1),
            followRedirects = true,
        )
        val request = Request.Builder()
            .url(targetUrl)
            .header("User-Agent", USER_AGENT)
            .get()
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) error("下载测速失败: HTTP ${response.code}")
            val colo = coloParser.parse(response.headers) ?: ping.colo
            val timeoutNanos = config.downloadTimeoutSeconds.coerceAtLeast(1) * 1_000_000_000L
            val startedAt = System.nanoTime()
            val buffer = ByteArray(16 * 1024)
            var totalBytes = 0L
            val stream = response.body.byteStream()
            while (currentCoroutineContext().isActive) {
                if (System.nanoTime() - startedAt >= timeoutNanos) break
                val read = stream.read(buffer)
                if (read <= 0) break
                totalBytes += read
            }
            val elapsedNanos = System.nanoTime() - startedAt
            val seconds = max(elapsedNanos / 1_000_000_000.0, 0.001)
            SpeedTestResult(
                candidate = ping.candidate,
                port = ping.port,
                sent = ping.sent,
                received = ping.received,
                lossRate = ping.lossRate,
                averageDelayMillis = ping.averageDelayMillis,
                downloadSpeedBytesPerSecond = totalBytes / seconds,
                colo = colo,
            )
        }
    }

    private fun withPort(url: String, port: Int): String {
        val uri = URI(url)
        return URI(uri.scheme, uri.userInfo, uri.host, port, uri.path, uri.query, uri.fragment).toString()
    }

    private companion object {
        const val USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/98.0.4758.80 Safari/537.36"
    }
}
