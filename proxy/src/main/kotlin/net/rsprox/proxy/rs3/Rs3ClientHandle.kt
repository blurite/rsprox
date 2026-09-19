package net.rsprox.proxy.rs3

import net.rsprox.proxy.rs3.relay.Rs3RelayServer
import java.util.concurrent.atomic.AtomicBoolean

public class Rs3ClientHandle(
    public val relayServer: Rs3RelayServer,
    public val proxyModulusHex: String,
    public val port: Int,
    private val onShutdown: () -> Unit = {},
) {
    private val closed = AtomicBoolean()

    public fun shutdown() {
        if (closed.compareAndSet(false, true)) relayServer.shutdown().whenComplete { _, _ -> onShutdown() }
    }
}
