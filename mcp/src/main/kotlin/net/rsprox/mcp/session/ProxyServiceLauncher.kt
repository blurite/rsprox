package net.rsprox.mcp.session

import net.rsprox.mcp.bridge.BridgeJar
import net.rsprox.proxy.ProxyService
import net.rsprox.proxy.connection.ClientTypeDictionary
import net.rsprox.proxy.target.ProxyTarget
import net.rsprox.proxy.target.ProxyTargetConfig
import java.io.IOException
import java.net.BindException
import java.net.ServerSocket
import java.util.concurrent.CompletionException
import java.util.stream.Collectors

/** Launches clients through a started [ProxyService]. */
internal class ProxyServiceLauncher(
    /** The proxy that launches the clients. */
    private val service: ProxyService,
    /** The installer of the bridge plugin. */
    private val bridgeJar: BridgeJar,
) : ClientLauncher {
    /** Get the proxy targets the proxy is configured with. */
    override fun targets(): List<ProxyTargetConfig> = service.proxyTargets

    /** Install the bridge plugin, pick a free proxy port and bind the HTTP server of the target. */
    override fun reserve(target: ProxyTargetConfig): Reservation {
        bridgeJar.installFor(target)

        // The proxy logs and returns when it cannot bind a proxy port, and a GUI may own any port in the range.
        val (port, proxyTarget) = firstBound(target)
        val forks = ForkWatch()

        return Reservation(
            port,
            proxyTarget.httpPort,
            launch = { monitor ->
                service.launchRuneLiteClient(monitor, null, port, proxyTarget)

                // Probing leaves a window in which another process can take the port. The proxy registers
                // a client type for a port only after it has bound it.
                check(hasClientType(port)) { "proxy port $port could not be bound" }
            },
            launcherExited = forks::allExited,
        )
    }

    /**
     * Get the first port of the proxy that is free and whose HTTP port can be bound, with the target that
     * was bound for it. Throws when none of [BIND_ATTEMPTS] ports works.
     */
    private fun firstBound(target: ProxyTargetConfig): Pair<Int, ProxyTarget> {
        repeat(BIND_ATTEMPTS) {
            val port = service.allocatePort()
            val bound = if (canBind(port)) bindHttpServer(port, target) else null

            if (bound != null) return port to bound
        }

        throw IllegalStateException("none of $BIND_ATTEMPTS proxy ports in a row could be bound with its HTTP port")
    }

    /** Kill the client on the proxy port, if any, and release the proxy state held for it. */
    override fun kill(proxyPort: Int) {
        service.killAliveProcess(proxyPort)
    }

    /** Bind the HTTP server that belongs to the proxy port, or return null when its port is taken. */
    private fun bindHttpServer(
        port: Int,
        target: ProxyTargetConfig,
    ): ProxyTarget? =
        try {
            service.initializeHttpServer(port, target)
        } catch (e: CompletionException) {
            // ProxyTarget.launchHttpServer joins the bind, which fails with the cause wrapped. Nothing
            // of the target is kept by the proxy at that point, so the next port starts clean.
            if (e.cause !is BindException) throw e

            null
        }

    /** Determine if the proxy registered a client type for the port, which it does once it has bound it. */
    private fun hasClientType(port: Int): Boolean =
        try {
            ClientTypeDictionary[port]
            true
        } catch (e: IllegalArgumentException) {
            false
        }

    /** Determine if the port can be bound right now, which it cannot while a process holds it. */
    private fun canBind(port: Int): Boolean =
        try {
            ServerSocket(port).close()
            true
        } catch (e: IOException) {
            false
        }
}

/** The most proxy ports that one reservation tries. */
private const val BIND_ATTEMPTS = 64

/**
 * The processes this JVM forks from the moment the watch is made. Launches take turns, so the forks that
 * follow a reservation are those of its launch.
 */
private class ForkWatch {
    /** The children that were running before the launch. */
    private val before = children()

    /** The processes of the launch seen so far. An exited child is no longer listed, so they are remembered. */
    private val seen = HashSet<ProcessHandle>()

    /**
     * Determine if every process forked since the watch was made has exited. False until one has been seen.
     * A process that starts and exits between two calls is never seen.
     */
    fun allExited(): Boolean {
        for (child in children() - before) {
            seen += child

            // A launcher may hand over to a client of its own and exit, which is not the launch dying.
            child.descendants().forEach { seen += it }
        }

        return seen.isNotEmpty() && seen.none { it.isAlive }
    }

    /** Get the direct children of this JVM that are running. */
    private fun children(): Set<ProcessHandle> = ProcessHandle.current().children().collect(Collectors.toSet())
}
