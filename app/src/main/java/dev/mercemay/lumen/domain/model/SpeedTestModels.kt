package dev.mercemay.lumen.domain.model

import java.net.InetAddress

enum class IpFamily {
    IPv4,
    IPv6,
}

data class IpCandidate(
    val hostAddress: String,
    val address: InetAddress,
    val family: IpFamily,
)

data class PingResult(
    val candidate: IpCandidate,
    val port: Int,
    val sent: Int,
    val received: Int,
    val averageDelayMillis: Double,
    val colo: String? = null,
) {
    val lossRate: Double
        get() = if (sent <= 0) 1.0 else (sent - received).toDouble() / sent.toDouble()
}

data class SpeedTestResult(
    val candidate: IpCandidate,
    val port: Int,
    val sent: Int,
    val received: Int,
    val lossRate: Double,
    val averageDelayMillis: Double,
    val downloadSpeedBytesPerSecond: Double,
    val colo: String? = null,
) {
    val downloadSpeedMbps: Double
        get() = downloadSpeedBytesPerSecond / 1024.0 / 1024.0
}

sealed interface TestProgress {
    data object Idle : TestProgress
    data class LoadingIps(val total: Int) : TestProgress
    data class Pinging(val completed: Int, val total: Int, val available: Int) : TestProgress
    data class Downloading(val completed: Int, val total: Int, val currentIp: String?) : TestProgress
    data class Completed(val results: Int) : TestProgress
    data class Failed(val message: String) : TestProgress
}
