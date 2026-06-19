package dev.mercemay.lumen.data.network

import android.net.Network
import java.net.InetAddress
import java.net.Socket
import javax.net.SocketFactory

class BoundSocketFactory(
    private val network: Network?,
) : SocketFactory() {
    private val delegate: SocketFactory = network?.socketFactory ?: SocketFactory.getDefault()

    override fun createSocket(): Socket = delegate.createSocket()

    override fun createSocket(host: String, port: Int): Socket = delegate.createSocket(host, port)

    override fun createSocket(host: String, port: Int, localHost: InetAddress, localPort: Int): Socket =
        delegate.createSocket(host, port, localHost, localPort)

    override fun createSocket(host: InetAddress, port: Int): Socket = delegate.createSocket(host, port)

    override fun createSocket(address: InetAddress, port: Int, localAddress: InetAddress, localPort: Int): Socket =
        delegate.createSocket(address, port, localAddress, localPort)
}
