package dev.mercemay.lumen.data.cfst

import dev.mercemay.lumen.domain.model.IpCandidate
import dev.mercemay.lumen.domain.model.IpFamily
import dev.mercemay.lumen.domain.model.SpeedTestResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.net.InetAddress

class FormatterTest {
    @Test
    fun `add txt wraps ipv6 with brackets`() {
        val result = SpeedTestResult(
            candidate = IpCandidate("2606:4700::1", InetAddress.getByName("2606:4700::1"), IpFamily.IPv6),
            port = 2053,
            sent = 4,
            received = 4,
            lossRate = 0.0,
            averageDelayMillis = 18.2,
            downloadSpeedBytesPerSecond = 12.5 * 1024 * 1024,
            colo = "NRT",
        )

        val text = AddTxtFormatter().format(listOf(result), port = 443, topN = 1)

        assertTrue(text.startsWith("[2606:4700::1]:2053#"))
    }

    @Test
    fun `csv uses cfst header`() {
        val csv = CsvFormatter().format(emptyList())
        assertEquals("IP 地址,已发送,已接收,丢包率,平均延迟,下载速度(MB/s),地区码\n", csv)
    }
}
