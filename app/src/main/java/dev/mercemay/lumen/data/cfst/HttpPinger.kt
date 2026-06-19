package dev.mercemay.lumen.data.cfst

import android.net.Network
import dev.mercemay.lumen.data.network.OkHttpClientFactory
import dev.mercemay.lumen.domain.model.IpCandidate
import dev.mercemay.lumen.domain.model.PingResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request
import java.net.URI
import kotlin.system.measureNanoTime

class HttpPinger(
    private val okHttpClientFactory: OkHttpClientFactory,
    private val coloParser: ColoParser = ColoParser(),
) {
    suspend fun pingOne(
        candidate: IpCandidate,
        url: String,
        port: Int,
        pingTimes: Int,
        statusCode: Int?,
        allowedColos: Set<String>,
        network: Network?,
    ): PingResult = withContext(Dispatchers.IO) {
        val uri = URI(url)
        val host = requireNotNull(uri.host) { "测速 URL 缺少 host: $url" }
        val targetUrl = withPort(url, port)
        val client = okHttpClientFactory.forCandidate(host, candidate.address, network, timeoutSeconds = 2, followRedirects = false)
        val request = Request.Builder().url(targetUrl).head().header("User-Agent", USER_AGENT).build()
        val first = runCatching { client.newCall(request).execute().use { it.code to coloParser.parse(it.headers) } }.getOrNull()
        if (first == null || !isStatusAccepted(first.first, statusCode)) {
            return@withContext PingResult(candidate, port, pingTimes.coerceAtLeast(1), 0, Double.MAX_VALUE)
        }
        val colo = first.second
        if (allowedColos.isNotEmpty() && colo !in allowedColos) {
            return@withContext PingResult(candidate, port, pingTimes.coerceAtLeast(1), 0, Double.MAX_VALUE, colo)
        }

        var received = 0
        var totalNanos = 0L
        repeat(pingTimes.coerceAtLeast(1)) { index ->
            val timedRequest = request.newBuilder()
                .apply { if (index == pingTimes - 1) header("Connection", "close") }
                .build()
            val elapsed = runCatching {
                measureNanoTime { client.newCall(timedRequest).execute().close() }
            }.getOrNull()
            if (elapsed != null) {
                received += 1
                totalNanos += elapsed
            }
        }
        PingResult(
            candidate = candidate,
            port = port,
            sent = pingTimes.coerceAtLeast(1),
            received = received,
            averageDelayMillis = if (received == 0) Double.MAX_VALUE else totalNanos / received / 1_000_000.0,
            colo = colo,
        )
    }

    private fun isStatusAccepted(code: Int, requested: Int?): Boolean {
        return if (requested == null || requested !in 100..599) {
            code == 200 || code == 301 || code == 302
        } else {
            code == requested
        }
    }

    private fun withPort(url: String, port: Int): String {
        val uri = URI(url)
        return URI(uri.scheme, uri.userInfo, uri.host, port, uri.path, uri.query, uri.fragment).toString()
    }

    private companion object {
        const val USER_AGENT = "LumenCFST/0.1 Android"
    }
}
