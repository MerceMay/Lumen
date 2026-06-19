package dev.mercemay.lumen.data.cfst

import dev.mercemay.lumen.domain.model.IpFamily
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class IpRangeParserTest {
    private val parser = IpRangeParser()

    @Test
    fun `single ipv4 becomes slash 32`() {
        val range = parser.parseOrNull("104.17.1.1")!!
        assertEquals(IpFamily.IPv4, range.family)
        assertEquals(32, range.prefixLength)
    }

    @Test
    fun `single ipv6 becomes slash 128`() {
        val range = parser.parseOrNull("2606:4700::1")!!
        assertEquals(IpFamily.IPv6, range.family)
        assertEquals(128, range.prefixLength)
    }

    @Test
    fun `sampler returns ipv4 candidates`() {
        val ranges = parser.parseLines("104.17.0.0/16")
        val candidates = IpSampler(kotlin.random.Random(1)).sample(ranges)
        assertEquals(256, candidates.size)
        assertTrue(candidates.all { it.family == IpFamily.IPv4 })
    }
}
