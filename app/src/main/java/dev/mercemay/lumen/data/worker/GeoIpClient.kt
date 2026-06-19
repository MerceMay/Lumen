package dev.mercemay.lumen.data.worker

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.InetAddress
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeoIpClient @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    @Volatile private var ranges: List<GeoRange>? = null

    suspend fun countryCode(ip: String): String? = withContext(Dispatchers.IO) {
        countryCodeBlocking(ip)
    }

    fun countryCodeBlocking(ip: String): String? {
        val address = runCatching { InetAddress.getByName(ip.removePrefix("[").removeSuffix("]")) }.getOrNull() ?: return null
        return loadRanges().firstOrNull { it.matches(address.address) }?.countryCode
    }

    private fun loadRanges(): List<GeoRange> {
        ranges?.let { return it }
        return synchronized(this) {
            ranges ?: GeoIpDatParser(loadGeoIpBytes()).parse().also { ranges = it }
        }
    }

    private fun loadGeoIpBytes(): ByteArray {
        val updated = File(context.filesDir, "geoip/geoip.dat")
        return if (updated.exists()) updated.readBytes() else context.assets.open("geoip/geoip.dat").use { it.readBytes() }
    }
}


private data class GeoRange(
    val countryCode: String,
    val ip: ByteArray,
    val prefix: Int,
) {
    fun matches(address: ByteArray): Boolean {
        if (address.size != ip.size) return false
        val fullBytes = prefix / 8
        val remainingBits = prefix % 8
        for (index in 0 until fullBytes) {
            if (address[index] != ip[index]) return false
        }
        if (remainingBits == 0) return true
        val mask = (0xFF shl (8 - remainingBits)) and 0xFF
        return (address[fullBytes].toInt() and mask) == (ip[fullBytes].toInt() and mask)
    }
}

private class GeoIpDatParser(
    private val data: ByteArray,
) {
    fun parse(): List<GeoRange> {
        val ranges = ArrayList<GeoRange>()
        val root = ProtoReader(data)
        while (!root.exhausted()) {
            when (root.readTag()) {
                10 -> parseGeoIp(root.readLengthDelimited(), ranges)
                else -> root.skipValueByTag()
            }
        }
        return ranges
    }

    private fun parseGeoIp(bytes: ByteArray, output: MutableList<GeoRange>) {
        val reader = ProtoReader(bytes)
        var countryCode = ""
        val cidrs = ArrayList<Pair<ByteArray, Int>>()
        while (!reader.exhausted()) {
            when (reader.readTag()) {
                10 -> countryCode = reader.readLengthDelimited().decodeToString().uppercase().removePrefix("GEOIP:")
                18 -> parseCidr(reader.readLengthDelimited())?.let { cidrs += it }
                else -> reader.skipValueByTag()
            }
        }
        if (countryCode.length == 2) {
            cidrs.forEach { (ip, prefix) -> output += GeoRange(countryCode, ip, prefix) }
        }
    }

    private fun parseCidr(bytes: ByteArray): Pair<ByteArray, Int>? {
        val reader = ProtoReader(bytes)
        var ip: ByteArray? = null
        var prefix: Int? = null
        while (!reader.exhausted()) {
            when (reader.readTag()) {
                10 -> ip = reader.readLengthDelimited()
                16 -> prefix = reader.readVarint().toInt()
                else -> reader.skipValueByTag()
            }
        }
        val value = ip ?: return null
        val bits = value.size * 8
        return value to (prefix ?: bits)
    }
}

private class ProtoReader(
    private val bytes: ByteArray,
) {
    private var position = 0
    private var lastTag = 0

    fun exhausted(): Boolean = position >= bytes.size

    fun readTag(): Int {
        lastTag = readVarint().toInt()
        return lastTag
    }

    fun readVarint(): Long {
        var shift = 0
        var result = 0L
        while (position < bytes.size) {
            val b = bytes[position++].toInt() and 0xFF
            result = result or ((b and 0x7F).toLong() shl shift)
            if ((b and 0x80) == 0) return result
            shift += 7
        }
        error("Invalid protobuf varint")
    }

    fun readLengthDelimited(): ByteArray {
        val length = readVarint().toInt()
        require(length >= 0 && position + length <= bytes.size) { "Invalid protobuf length" }
        return bytes.copyOfRange(position, position + length).also { position += length }
    }

    fun skipValueByTag() {
        when (lastTag and 0x07) {
            0 -> readVarint()
            1 -> position += 8
            2 -> position += readVarint().toInt()
            5 -> position += 4
            else -> error("Unsupported protobuf wire type: ${lastTag and 0x07}")
        }
        require(position <= bytes.size) { "Invalid protobuf skip" }
    }
}
