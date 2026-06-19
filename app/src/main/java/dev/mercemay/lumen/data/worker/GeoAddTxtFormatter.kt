package dev.mercemay.lumen.data.worker

import dev.mercemay.lumen.domain.model.IpFamily
import dev.mercemay.lumen.domain.model.SpeedTestResult
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeoAddTxtFormatter @Inject constructor(
    private val geoIpClient: GeoIpClient,
) {
    suspend fun format(results: List<SpeedTestResult>, topN: Int = 50): String {
        val grouped = LinkedHashMap<String, MutableList<SpeedTestResult>>()
        results.take(topN).forEach { result ->
            val country = geoIpClient.countryCode(result.candidate.hostAddress) ?: UNKNOWN
            grouped.getOrPut(country) { mutableListOf() } += result
        }
        return grouped.flatMap { (country, items) ->
            items.mapIndexed { index, result ->
                val host = when (result.candidate.family) {
                    IpFamily.IPv4 -> result.candidate.hostAddress
                    IpFamily.IPv6 -> "[${result.candidate.hostAddress.removePrefix("[").removeSuffix("]")}]"
                }
                "$host:${result.port}#$country${index + 1}"
            }
        }.joinToString("\n")
    }

    private companion object {
        const val UNKNOWN = "Unknown"
    }
}
