package net.rsprox.proxy.rs3

import net.rsprox.proxy.rs3.http.Rs3JavConfigHttpServer
import net.rsprox.proxy.rs3.relay.Rs3RelayServer

public class Rs3ClientHandle(
    public val httpServer: Rs3JavConfigHttpServer,
    public val relayServer: Rs3RelayServer,
    public val proxyModulusHex: String,
    public val port: Int,
) {
    public fun shutdown() {
        httpServer.shutdown()
        relayServer.shutdown()
    }
}
