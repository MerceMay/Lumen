package dev.mercemay.lumen.data.cfst

import android.content.Context
import dev.mercemay.lumen.domain.model.IpMode

object BuiltInIpSources {
    fun load(context: Context, ipMode: IpMode): String {
        val ipv4 = readAsset(context, "cfst/ip.txt")
        val ipv6 = readAsset(context, "cfst/ipv6.txt")
        return when (ipMode) {
            IpMode.IPv4 -> ipv4
            IpMode.IPv6 -> ipv6
            IpMode.DualStack -> "$ipv4\n$ipv6"
        }
    }

    private fun readAsset(context: Context, path: String): String = context.assets.open(path).bufferedReader().use { it.readText() }
}
