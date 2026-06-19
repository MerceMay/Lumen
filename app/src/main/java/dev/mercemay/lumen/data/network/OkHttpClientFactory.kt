package dev.mercemay.lumen.data.network

import android.net.Network
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import okhttp3.Protocol
import java.net.InetAddress
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class OkHttpClientFactory @Inject constructor() {
    fun forCandidate(
        candidate: InetAddress,
        network: Network?,
        timeoutSeconds: Long,
        followRedirects: Boolean,
    ): OkHttpClient = OkHttpClient.Builder()
        .dns(CandidateIpDns(candidate))
        .socketFactory(BoundSocketFactory(network))
        .connectionPool(ConnectionPool(0, 1, TimeUnit.NANOSECONDS))
        .protocols(listOf(Protocol.HTTP_1_1))
        .followRedirects(followRedirects)
        .followSslRedirects(followRedirects)
        .connectTimeout(timeoutSeconds, TimeUnit.SECONDS)
        .readTimeout(timeoutSeconds, TimeUnit.SECONDS)
        .build()

    fun default(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()
}
