package net.rsprox.proxy.connection

import io.netty.channel.Channel
import net.rsprox.proxy.binary.BinaryBlob

public class ProxyConnectionContainer {
    private val connections: MutableList<ProxyConnection> = mutableListOf()

    public fun addConnection(
        clientChannel: Channel,
        serverChannel: Channel,
        blob: BinaryBlob,
    ) {
        this.connections +=
            ProxyConnection(
                clientChannel,
                serverChannel,
                blob,
            )
    }

    public fun listConnections(): List<ProxyConnection> {
        return connections
    }
}
