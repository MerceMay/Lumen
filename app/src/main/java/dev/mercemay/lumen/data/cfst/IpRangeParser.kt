package dev.mercemay.lumen.data.cfst

import dev.mercemay.lumen.domain.model.IpFamily
import java.math.BigInteger
import java.net.Inet4Address
import java.net.Inet6Address
import java.net.InetAddress

data class IpRange(
    val base: InetAddress,
    val prefixLength: Int,
    val family: IpFamily,
)

class IpRangeParser {
    fun parseLines(text: String): List<IpRange> = text
        .lineSequence()
        .flatMap { it.splitToSequence(',', ' ', '\t') }
        .map { it.trim() }
        .filter { it.isNotEmpty() && !it.startsWith("#") }
        .mapNotNull { parseOrNull(it) }
        .toList()

    fun parseOrNull(input: String): IpRange? {
        val normalized = input.trim().removePrefix("[").removeSuffix("]")
        if (normalized.isEmpty()) return null

        val slashIndex = normalized.lastIndexOf('/')
        val addressText = if (slashIndex >= 0) normalized.substring(0, slashIndex) else normalized
        val address = runCatching { InetAddress.getByName(addressText) }.getOrNull() ?: return null
        val family = when (address) {
            is Inet4Address -> IpFamily.IPv4
            is Inet6Address -> IpFamily.IPv6
            else -> return null
        }
        val maxPrefix = if (family == IpFamily.IPv4) 32 else 128
        val prefix = if (slashIndex >= 0) {
            normalized.substring(slashIndex + 1).toIntOrNull() ?: return null
        } else {
            maxPrefix
        }
        if (prefix !in 0..maxPrefix) return null
        return IpRange(address, prefix, family)
    }
}

internal fun InetAddress.toPositiveBigInteger(): BigInteger = BigInteger(1, address)

internal fun BigInteger.toInetAddress(family: IpFamily): InetAddress {
    val size = if (family == IpFamily.IPv4) 4 else 16
    val raw = toByteArray()
    val bytes = ByteArray(size)
    val copyStart = maxOf(0, raw.size - size)
    val copyLength = raw.size - copyStart
    System.arraycopy(raw, copyStart, bytes, size - copyLength, copyLength)
    return InetAddress.getByAddress(bytes)
}

internal fun networkAddress(range: IpRange): BigInteger {
    val bits = if (range.family == IpFamily.IPv4) 32 else 128
    val hostBits = bits - range.prefixLength
    val address = range.base.toPositiveBigInteger()
    return if (hostBits == 0) address else address.shiftRight(hostBits).shiftLeft(hostBits)
}
