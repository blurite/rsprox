package net.rsprox.proxy.rs3

import net.rsprox.proxy.rs3.relay.Rs3RelayServer

public class Rs3ClientHandle(
    public val relayServer: Rs3RelayServer,
    public val proxyModulusHex: String,
    public val port: Int,
) {
    public fun shutdown() {
        relayServer.shutdown()
    }
}
