package dev.mercemay.lumen.data.network

import okhttp3.Dns
import java.net.InetAddress

class CandidateIpDns(
    private val targetHost: String,
    private val candidate: InetAddress,
) : Dns {
    override fun lookup(hostname: String): List<InetAddress> {
        return if (hostname.equals(targetHost, ignoreCase = true)) {
            listOf(candidate)
        } else {
            Dns.SYSTEM.lookup(hostname)
        }
    }
}
