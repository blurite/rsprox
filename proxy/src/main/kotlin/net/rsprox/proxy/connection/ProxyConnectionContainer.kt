package net.rsprox.proxy.connection

import io.netty.channel.Channel
import net.rsprox.proxy.binary.BinaryBlob
import net.rsprox.proxy.binary.BinaryHeader
import net.rsprox.proxy.unix.UnixSocketConnection
import net.rsprox.shared.SessionMonitor
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

public class ProxyConnectionContainer {
    private val connections: MutableList<ProxyConnection> = mutableListOf()
    private val sessionMonitors: MutableMap<Int, SessionMonitor<BinaryHeader>> = mutableMapOf()
    private val cleanupExecutor = Executors.newSingleThreadScheduledExecutor()
    private val unixConnections: MutableMap<Int, UnixSocketConnection> = ConcurrentHashMap()

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

    public fun addUnixConnection(
        httpPort: Int,
        connection: UnixSocketConnection,
    ) {
        val old = unixConnections.put(httpPort, connection)
        check(old == null) {
            "Overlapping unix connection: $httpPort, $connection, $old"
        }
    }

    public fun removeUnixConnection(connection: UnixSocketConnection) {
        unixConnections.entries.removeIf { (_, con) ->
            con === connection
        }
    }

    public fun getUnixConnectionOrNull(httpPort: Int): UnixSocketConnection? {
        return unixConnections[httpPort]
    }

    public fun removeConnection(blob: BinaryBlob) {
        val closeTimestamp = blob.closeTimestamp
        if (closeTimestamp == 0L) return
        cleanupExecutor.schedule(
            {
                // If a reconnect didn't take place in the past minute, clean it up.
                // This will release the cache and all the transcriptions in memory.
                if (blob.closeTimestamp == closeTimestamp) {
                    // Remove any equal-by-reference
                    connections.removeIf { it.blob === blob }
                    // Finally, shut the blob down fully so the thread is cleaned up
                    blob.shutdown()
                }
            },
            1L,
            TimeUnit.MINUTES,
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

    internal fun removeSessionMonitor(port: Int) {
        this.sessionMonitors.remove(port)
    }

    public fun getSessionMonitor(port: Int): SessionMonitor<BinaryHeader> {
        return this.sessionMonitors[port]
            ?: throw IllegalArgumentException("Session monitor not set for port $port")
    }

    public fun listConnections(): List<ProxyConnection> {
        return connections
    }
}
