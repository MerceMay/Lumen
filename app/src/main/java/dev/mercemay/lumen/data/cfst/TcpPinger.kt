package dev.mercemay.lumen.data.cfst

import dev.mercemay.lumen.domain.model.IpCandidate
import dev.mercemay.lumen.domain.model.PingResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.net.InetSocketAddress
import java.util.concurrent.atomic.AtomicInteger
import javax.net.SocketFactory
import kotlin.system.measureNanoTime

class TcpPinger {
    suspend fun pingAll(
        candidates: List<IpCandidate>,
        ports: List<Int>,
        pingTimes: Int,
        concurrency: Int,
        socketFactory: SocketFactory,
        connectTimeoutMillis: Int = 1_000,
        onProgress: (completed: Int, total: Int, available: Int) -> Unit = { _, _, _ -> },
    ): List<PingResult> = coroutineScope {
        require(ports.isNotEmpty()) { "测速端口不能为空" }
        val targets = candidates.flatMap { candidate -> ports.map { port -> candidate to port } }
        val nextIndex = AtomicInteger(0)
        val completed = AtomicInteger(0)
        val available = AtomicInteger(0)
        val results = ArrayList<PingResult>(targets.size)
        val mutex = Mutex()
        val workerCount = concurrency.coerceIn(1, targets.size.coerceAtLeast(1))

        List(workerCount) {
            async(Dispatchers.IO) {
                while (true) {
                    val index = nextIndex.getAndIncrement()
                    if (index >= targets.size) break
                    val (candidate, port) = targets[index]
                    val result = pingOne(candidate, port, pingTimes, socketFactory, connectTimeoutMillis)
                    mutex.withLock { results += result }
                    val done = completed.incrementAndGet()
                    if (result.received > 0) available.incrementAndGet()
                    onProgress(done, targets.size, available.get())
                }
            }
        }.awaitAll()
        results
    }

    private suspend fun pingOne(
        candidate: IpCandidate,
        port: Int,
        pingTimes: Int,
        socketFactory: SocketFactory,
        connectTimeoutMillis: Int,
    ): PingResult = withContext(Dispatchers.IO) {
        var received = 0
        var totalNanos = 0L
        repeat(pingTimes.coerceAtLeast(1)) {
            val elapsed = runCatching {
                var nanos = 0L
                socketFactory.createSocket().use { socket ->
                    nanos = measureNanoTime {
                        socket.connect(InetSocketAddress(candidate.address, port), connectTimeoutMillis)
                    }
                }
                nanos
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
        )
    }
}
