package dev.mercemay.lumen.domain.model

enum class IpMode {
    IPv4,
    IPv6,
    DualStack,
}

enum class TestStrategy {
    TCPing,
    HTTPing,
}

data class SpeedTestConfig(
    val pingConcurrency: Int = 200,
    val pingTimes: Int = 4,
    val downloadTestCount: Int = 50,
    val downloadTimeoutSeconds: Int = 10,
    val port: Int = 443,
    val testUrl: String = "https://cf.xiu2.xyz/url",
    val testStrategy: TestStrategy = TestStrategy.TCPing,
    val httpingStatusCode: Int? = null,
    val cfColoFilter: Set<String> = emptySet(),
    val maxDelayMillis: Long = 9999,
    val minDelayMillis: Long = 0,
    val maxLossRate: Double = 1.0,
    val minDownloadSpeedMbps: Double = 0.0,
    val disableDownload: Boolean = false,
    val ipMode: IpMode = IpMode.IPv4,
    val ipText: String = "",
)
