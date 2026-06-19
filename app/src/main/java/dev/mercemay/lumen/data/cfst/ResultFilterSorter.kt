package dev.mercemay.lumen.data.cfst

import dev.mercemay.lumen.domain.model.PingResult
import dev.mercemay.lumen.domain.model.SpeedTestConfig
import dev.mercemay.lumen.domain.model.SpeedTestResult

class ResultFilterSorter {
    fun sortPingResults(results: List<PingResult>): List<PingResult> = results.sortedWith(
        compareBy<PingResult> { it.lossRate }
            .thenBy { it.averageDelayMillis }
    )

    fun filterPingResults(results: List<PingResult>, config: SpeedTestConfig): List<PingResult> = results
        .asSequence()
        .filter { it.averageDelayMillis >= config.minDelayMillis }
        .filter { it.averageDelayMillis <= config.maxDelayMillis }
        .filter { it.lossRate <= config.maxLossRate }
        .toList()

    fun sortSpeedResults(results: List<SpeedTestResult>): List<SpeedTestResult> = results.sortedByDescending {
        it.downloadSpeedBytesPerSecond
    }
}
