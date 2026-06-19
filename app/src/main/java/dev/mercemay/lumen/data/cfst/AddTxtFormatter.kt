package dev.mercemay.lumen.data.cfst

import dev.mercemay.lumen.domain.model.IpFamily
import dev.mercemay.lumen.domain.model.SpeedTestResult

class AddTxtFormatter {
    fun format(results: List<SpeedTestResult>, port: Int = 443, topN: Int): String = results
        .take(topN)
        .mapIndexed { index, result ->
            val host = when (result.candidate.family) {
                IpFamily.IPv4 -> result.candidate.hostAddress
                IpFamily.IPv6 -> "[${result.candidate.hostAddress.removePrefix("[").removeSuffix("]")}]"
            }
            "$host:${result.port}#${ColoFlag.Unknown}${index + 1}"
        }
        .joinToString(separator = "\n")
}
