package dev.mercemay.lumen.data.cfst

import dev.mercemay.lumen.domain.model.SpeedTestResult
import java.util.Locale

class CsvFormatter {
    fun format(results: List<SpeedTestResult>): String = buildString {
        appendLine("IP 地址,已发送,已接收,丢包率,平均延迟,下载速度(MB/s),地区码")
        results.forEach { result ->
            append(result.candidate.hostAddress)
            append(',')
            append(result.sent)
            append(',')
            append(result.received)
            append(',')
            append(String.format(Locale.US, "%.2f", result.lossRate))
            append(',')
            append(String.format(Locale.US, "%.2f", result.averageDelayMillis))
            append(',')
            append(String.format(Locale.US, "%.2f", result.downloadSpeedMbps))
            append(',')
            append(result.colo ?: "N/A")
            appendLine()
        }
    }
}
