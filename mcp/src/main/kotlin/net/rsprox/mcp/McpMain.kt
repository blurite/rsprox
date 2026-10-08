package net.rsprox.mcp

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.switch
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.parameters.types.path
import com.github.michaelbull.logging.InlineLogger
import io.netty.buffer.ByteBufAllocator
import net.rsprox.mcp.bridge.BridgeHub
import net.rsprox.mcp.bridge.BridgeJar
import net.rsprox.mcp.bridge.Rendering
import net.rsprox.mcp.packets.TapSettingSetStore
import net.rsprox.mcp.packets.UnfilteredFilterSetStore
import net.rsprox.mcp.server.ToolError
import net.rsprox.mcp.session.ProxyServiceLauncher
import net.rsprox.mcp.session.SessionManager
import net.rsprox.proxy.ProxyService
import net.rsprox.proxy.config.DEFAULT_MCP_PORT
import java.nio.file.Path
import java.util.Locale
import java.util.concurrent.atomic.AtomicReference
import kotlin.system.exitProcess

/** Runs rsprox without its GUI and serves it to an agent as an MCP server on loopback. */
public class McpCommand : CliktCommand(name = "mcp") {
    /** The loopback port of the MCP endpoint. */
    private val port by option("--port", help = "Loopback port of the MCP endpoint").int().default(DEFAULT_MCP_PORT)

    /** The number of proxy ports left unused at the start of the range. */
    private val portSkip by option(
        "--port-skip",
        help = "Proxy ports to leave unused at the start of the range, for a GUI running at the same time",
    ).int().default(50)

    /** The directory the client sideloads plugins from, or null for the default of the target. */
    private val sideloadDir by option(
        "--sideload-dir",
        help = "Directory the client sideloads plugins from, when it is not the default of the target",
    ).path()

    /** The rendering of the launched clients. */
    private val rendering by option(
        help = "Stop the GPU plugin in launched clients, for a virtual display such as Xvfb",
    ).switch("--software-rendering" to Rendering.SOFTWARE).default(Rendering.GPU)

    /** The name of the target to launch at startup, or null to launch none. */
    private val autostart by option("--start", help = "Target name to launch immediately")

    /** Serve the MCP endpoint, start the proxy and the bridge hub, and run until the process is killed. */
    override fun run() {
        Locale.setDefault(Locale.US)
        val sessions = AtomicReference<SessionManager?>()
        val http = endpoint(port, sessions)

        // Binding first makes a second instance exit here, before it touches the shared configuration.
        http.start()
        logger.info { "MCP endpoint listening on http://127.0.0.1:$port/mcp" }

        val manager =
            try {
                closingOnFailure { opened ->
                    opened += http
                    startSessions(opened)
                }
            } catch (t: Throwable) {
                // The proxy leaves threads behind that are not daemons, so returning would keep the process alive.
                logger.error(t) { "rsprox could not start" }
                exitProcess(1)
            }

        sessions.set(manager)
        logger.info { "Ready. Targets: ${manager.targets().joinToString(", ")}" }

        autostart?.let { target ->
            try {
                manager.start(target, null, SessionManager.DEFAULT_WAIT_MS)
            } catch (e: ToolError) {
                logger.error { "Unable to start '$target': ${e.message}" }
            }
        }

        Thread.currentThread().join()
    }

    /** Start the proxy and the bridge hub, and add what must be closed when a later step fails to [opened]. */
    private fun startSessions(opened: MutableList<AutoCloseable>): SessionManager {
        val service = ProxyService(ByteBufAllocator.DEFAULT)
        service.start(null, null) { percentage, _, subActionText, _ ->
            logger.debug { "Starting proxy service: $subActionText (${(percentage * 100).toInt()}%)" }
        }

        service.filterSetStore = UnfilteredFilterSetStore
        service.settingsStore = TapSettingSetStore

        return sessionManager(service, portSkip.coerceAtLeast(1), sideloadDir, rendering, opened)
    }

    private companion object {
        /** The logger of the command. */
        private val logger = InlineLogger()
    }
}

/**
 * Start the bridge hub and build the sessions of a started proxy, with the stores the proxy holds at
 * this moment. Adds what must be closed when a later step fails to [opened].
 */
internal fun sessionManager(
    service: ProxyService,
    portSkip: Int,
    sideloadDir: Path?,
    rendering: Rendering,
    opened: MutableList<AutoCloseable>,
): SessionManager {
    val launcher = ProxyServiceLauncher(service, portSkip, BridgeJar(sideloadDir))

    // The plugin reads the same path in McpBridgePlugin.java.
    val rendezvous = Path.of(System.getProperty("user.home"), ".rsprox", "mcp", "bridge.json")
    val hub = BridgeHub(rendezvous, rendering)

    opened += hub
    hub.start()

    // The proxy's own hook kills the clients; this one removes the rendezvous file they dial through.
    Runtime.getRuntime().addShutdownHook(Thread(hub::close, "mcp-bridge-shutdown"))

    return SessionManager(launcher, service.settingsStore, hub)
}

/**
 * Run the startup steps in [body], and close what they added to the list, newest first, when one of them fails.
 * The failure is passed on.
 */
internal fun <T> closingOnFailure(body: (opened: MutableList<AutoCloseable>) -> T): T {
    val opened = ArrayList<AutoCloseable>()

    try {
        return body(opened)
    } catch (t: Throwable) {
        for (closeable in opened.asReversed()) {
            try {
                closeable.close()
            } catch (e: Exception) {
                t.addSuppressed(e)
            }
        }

        throw t
    }
}

/** Run the MCP command with the arguments of the process. */
public fun main(args: Array<String>): Unit = McpCommand().main(args)
