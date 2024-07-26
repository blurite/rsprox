package net.rsprox.proxy.connection

import io.netty.channel.Channel
import net.rsprox.proxy.binary.BinaryBlob
import net.rsprox.proxy.binary.BinaryHeader
import net.rsprox.shared.SessionMonitor

public class ProxyConnectionContainer {
    private val connections: MutableList<ProxyConnection> = mutableListOf()
    private val sessionMonitors: MutableMap<Int, SessionMonitor<BinaryHeader>> = mutableMapOf()

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

    internal fun addSessionMonitor(
        port: Int,
        sessionMonitor: SessionMonitor<BinaryHeader>,
    ) {
        val old = this.sessionMonitors.put(port, sessionMonitor)
        check(old == null) {
            "Overwriting existing session monitor on port $port: $old"
        }
    }

    public fun getSessionMonitor(port: Int): SessionMonitor<BinaryHeader> {
        return this.sessionMonitors[port]
            ?: throw IllegalArgumentException("Session monitor not set for port $port")
    }

    public fun listConnections(): List<ProxyConnection> {
        return connections
    }
}
