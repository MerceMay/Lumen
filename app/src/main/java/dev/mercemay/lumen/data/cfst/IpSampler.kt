package dev.mercemay.lumen.data.cfst

import dev.mercemay.lumen.domain.model.IpCandidate
import dev.mercemay.lumen.domain.model.IpFamily
import kotlin.math.min
import kotlin.random.Random

class IpSampler(
    private val random: Random = Random.Default,
) {
    fun sample(ranges: List<IpRange>): List<IpCandidate> {
        val candidates = ArrayList<IpCandidate>()
        for (range in ranges) {
            when (range.family) {
                IpFamily.IPv4 -> sampleIpv4(range, candidates)
                IpFamily.IPv6 -> sampleIpv6(range, candidates)
            }
        }
        return candidates.distinctBy { it.hostAddress }
    }

    private fun sampleIpv4(range: IpRange, output: MutableList<IpCandidate>) {
        val network = networkAddress(range).toLong()
        val hostBits = 32 - range.prefixLength
        if (hostBits == 0) {
            addCandidate(network, IpFamily.IPv4, output)
            return
        }
        val count = 1L shl min(maxOf(0, hostBits - 8), 30)
        repeat(count.toInt()) { index ->
            val subnetOffset = index.toLong() shl 8
            val lastOctet = random.nextInt(1, 255).toLong()
            addCandidate(network + subnetOffset + lastOctet, IpFamily.IPv4, output)
        }
    }

    private fun sampleIpv6(range: IpRange, output: MutableList<IpCandidate>) {
        val network = networkAddress(range)
        val hostBits = 128 - range.prefixLength
        if (hostBits == 0) {
            val address = network.toInetAddress(IpFamily.IPv6)
            output += IpCandidate(requireNotNull(address.hostAddress), address, IpFamily.IPv6)
            return
        }
        val randomHost = randomHost(hostBits)
        val address = network.add(randomHost).toInetAddress(IpFamily.IPv6)
        output += IpCandidate(requireNotNull(address.hostAddress), address, IpFamily.IPv6)
    }

    private fun randomHost(hostBits: Int): java.math.BigInteger {
        val bytes = ByteArray((hostBits + 7) / 8)
        random.nextBytes(bytes)
        val excessBits = bytes.size * 8 - hostBits
        if (bytes.isNotEmpty() && excessBits > 0) {
            val mask = 0xFF ushr excessBits
            bytes[0] = (bytes[0].toInt() and mask).toByte()
        }
        return java.math.BigInteger(1, bytes)
    }

    private fun addCandidate(value: Long, family: IpFamily, output: MutableList<IpCandidate>) {
        val address = java.math.BigInteger.valueOf(value).toInetAddress(family)
        output += IpCandidate(requireNotNull(address.hostAddress), address, family)
    }
}
