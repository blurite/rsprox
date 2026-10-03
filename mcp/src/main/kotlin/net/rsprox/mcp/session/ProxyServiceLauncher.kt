package net.rsprox.mcp.session

import net.rsprox.mcp.bridge.BridgeJar
import net.rsprox.proxy.ProxyService
import net.rsprox.proxy.connection.ClientTypeDictionary
import net.rsprox.proxy.target.ProxyTargetConfig
import java.io.IOException
import java.net.ServerSocket

/** Must be created right after [ProxyService.start], before anything else allocates a port. */
internal class ProxyServiceLauncher(
    private val service: ProxyService,
    portSkip: Int,
    private val bridgeJar: BridgeJar,
) : ClientLauncher {
    // The proxy keeps its first proxy port private, and its HTTP ports are offsets from it.
    // The first allocation after start() returns that port.
    private val basePort = service.allocatePort()

    init {
        // Leaves the low ports to a GUI that shares the same configured range.
        repeat(portSkip - 1) { service.allocatePort() }
    }

    override fun targets(): List<ProxyTargetConfig> = service.proxyTargets

    override fun reserve(target: ProxyTargetConfig): Reservation {
        bridgeJar.installFor(target)

        // The proxy logs and returns when it cannot bind a proxy port, and a GUI may own any port in the range.
        var port: Int

        do {
            port = service.allocatePort()
        } while (!isFree(port) || !isFree(HTTP_PORT_BASE + (port - basePort)))

        val proxyTarget = service.initializeHttpServer(port, target)

        return Reservation(port, proxyTarget.httpPort) { monitor ->
            service.launchRuneLiteClient(monitor, null, port, proxyTarget)

            // Probing leaves a window in which another process can take the port. The proxy registers
            // a client type for a port only after it has bound it.
            check(isBound(port)) { "proxy port $port could not be bound" }
        }
    }

    override fun kill(proxyPort: Int) {
        service.killAliveProcess(proxyPort)
    }

    private fun isBound(port: Int): Boolean =
        try {
            ClientTypeDictionary[port]
            true
        } catch (e: IllegalArgumentException) {
            false
        }

    private fun isFree(port: Int): Boolean =
        try {
            ServerSocket(port).close()
            true
        } catch (e: IOException) {
            false
        }

    private companion object {
        // Mirrors net.rsprox.proxy.config.HTTP_SERVER_PORT, which is internal to the proxy module.
        private const val HTTP_PORT_BASE = 43600
    }
}
