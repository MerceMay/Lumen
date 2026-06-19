package dev.mercemay.lumen.data.cfst

import android.net.Network
import dev.mercemay.lumen.data.network.BoundSocketFactory
import dev.mercemay.lumen.domain.model.SpeedTestConfig
import dev.mercemay.lumen.domain.model.SpeedTestResult
import dev.mercemay.lumen.domain.model.TestProgress
import dev.mercemay.lumen.domain.model.TestStrategy
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import javax.inject.Inject

class SpeedTestEngine @Inject constructor(
    private val ipRangeParser: IpRangeParser,
    private val ipSampler: IpSampler,
    private val tcpPinger: TcpPinger,
    private val httpPinger: HttpPinger,
    private val downloadTester: DownloadTester,
    private val resultFilterSorter: ResultFilterSorter,
) {
    fun run(
        ipText: String,
        config: SpeedTestConfig,
        network: Network?,
    ): Flow<SpeedTestEvent> = channelFlow {
        val ranges = ipRangeParser.parseLines(ipText)
        send(SpeedTestEvent.Progress(TestProgress.LoadingIps(ranges.size)))
        val candidates = ipSampler.sample(ranges)
        val ports = listOf(config.port.coerceIn(1, 65535))

        suspend fun runTcp(targets: List<dev.mercemay.lumen.domain.model.IpCandidate>) = tcpPinger.pingAll(
            candidates = targets,
            ports = ports,
            pingTimes = config.pingTimes,
            concurrency = config.pingConcurrency,
            socketFactory = BoundSocketFactory(network),
        ) { completed, total, available ->
            trySend(SpeedTestEvent.Progress(TestProgress.Pinging(completed, total, available)))
        }.also { results ->
            send(SpeedTestEvent.Progress(TestProgress.Pinging(results.size, targets.size * ports.size, results.count { it.received > 0 })))
        }

        suspend fun runHttp(targets: List<dev.mercemay.lumen.domain.model.IpCandidate>): List<dev.mercemay.lumen.domain.model.PingResult> {
            val pairs = targets.flatMap { candidate -> ports.map { port -> candidate to port } }
            val results = ArrayList<dev.mercemay.lumen.domain.model.PingResult>()
            send(SpeedTestEvent.Progress(TestProgress.Pinging(0, pairs.size, 0)))
            pairs.forEachIndexed { index, (candidate, port) ->
                val result = httpPinger.pingOne(
                    candidate = candidate,
                    url = config.testUrl,
                    port = port,
                    pingTimes = config.pingTimes,
                    statusCode = config.httpingStatusCode,
                    allowedColos = config.cfColoFilter,
                    network = network,
                )
                results += result
                send(SpeedTestEvent.Progress(TestProgress.Pinging(index + 1, pairs.size, results.count { it.received > 0 })))
            }
            return results
        }

        val pingResults = when (config.testStrategy) {
            TestStrategy.TCPing -> runTcp(candidates)
            TestStrategy.HTTPing -> runHttp(candidates)
        }
        val filtered = resultFilterSorter.filterPingResults(resultFilterSorter.sortPingResults(pingResults), config)
        val speedResults = downloadTester.test(filtered, config, network) { completed, total, currentIp ->
            trySend(SpeedTestEvent.Progress(TestProgress.Downloading(completed, total, currentIp)))
        }
        val sorted = resultFilterSorter.sortSpeedResults(speedResults)
        send(SpeedTestEvent.Progress(TestProgress.Completed(sorted.size)))
        send(SpeedTestEvent.Completed(sorted))
    }
}

sealed interface SpeedTestEvent {
    data class Progress(val progress: TestProgress) : SpeedTestEvent
    data class Completed(val results: List<SpeedTestResult>) : SpeedTestEvent
}
