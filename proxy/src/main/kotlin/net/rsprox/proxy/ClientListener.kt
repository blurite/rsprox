package net.rsprox.proxy

import net.rsprox.proxy.binary.BinaryHeader
import net.rsprox.proxy.target.ProxyTargetConfig
import net.rsprox.shared.SessionMonitor

/** Hears of every RuneLite and native client that is launched with a session monitor, whoever launched it. */
public interface ClientListener {
    /**
     * Called before the client on [port] is started. A monitor that is returned hears every callback
     * of that client, after the monitor of whoever launched it.
     */
    public fun onClientLaunch(
        port: Int,
        target: ProxyTargetConfig,
    ): SessionMonitor<BinaryHeader>?

    /**
     * Called when the client on [port] was killed, failed to launch or exited. It may be called more
     * than once for a client, and for a port that no client was launched on.
     */
    public fun onClientClosed(port: Int)
}
