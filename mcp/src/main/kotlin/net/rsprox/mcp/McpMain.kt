package net.rsprox.mcp

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.parameters.types.path
import com.github.michaelbull.logging.InlineLogger
import io.netty.buffer.ByteBufAllocator
import net.rsprox.mcp.bridge.BridgeHub
import net.rsprox.mcp.bridge.BridgeJar
import net.rsprox.mcp.bridge.Rendering
import net.rsprox.mcp.packets.TapSettingSetStore
import net.rsprox.mcp.packets.UnfilteredFilterSetStore
import net.rsprox.mcp.server.McpHttpServer
import net.rsprox.mcp.server.ToolError
import net.rsprox.mcp.server.tools
import net.rsprox.mcp.session.ProxyServiceLauncher
import net.rsprox.mcp.session.SessionManager
import net.rsprox.proxy.ProxyService
import java.nio.file.Path
import java.util.Locale
import java.util.concurrent.atomic.AtomicReference

/** Runs rsprox without its GUI and serves it to an agent as an MCP server on loopback. */
public class McpCommand : CliktCommand(name = "mcp") {
    private val port by option("--port", help = "Loopback port of the MCP endpoint").int().default(43580)
    private val portSkip by option(
        "--port-skip",
        help = "Proxy ports to leave unused at the start of the range, for a GUI running at the same time",
    ).int().default(50)
    private val sideloadDir by option(
        "--sideload-dir",
        help = "Directory the client sideloads plugins from, when it is not the default of the target",
    ).path()
    private val softwareRendering by option(
        "--software-rendering",
        help = "Stop the GPU plugin in launched clients, for a virtual display such as Xvfb",
    ).flag()
    private val autostart by option("--start", help = "Target name to launch immediately")

    override fun run() {
        Locale.setDefault(Locale.US)
        val sessions = AtomicReference<SessionManager?>()
        val http =
            McpHttpServer(
                port,
                tools { sessions.get() ?: throw ToolError("rsprox is still starting; try again shortly") },
                System.getenv("APP_VERSION") ?: "dev",
            )

        // Binding first makes a second instance exit here, before it touches the shared configuration.
        http.start()
        logger.info { "MCP endpoint listening on http://127.0.0.1:$port/mcp" }

        val service = ProxyService(ByteBufAllocator.DEFAULT)
        service.start(null, null) { percentage, _, subActionText, _ ->
            logger.debug { "Starting proxy service: $subActionText (${(percentage * 100).toInt()}%)" }
        }

        service.filterSetStore = UnfilteredFilterSetStore
        service.settingsStore = TapSettingSetStore
        val launcher = ProxyServiceLauncher(service, portSkip.coerceAtLeast(1), BridgeJar(sideloadDir))

        // The plugin reads the same path in McpBridgePlugin.java.
        val rendezvous = Path.of(System.getProperty("user.home"), ".rsprox", "mcp", "bridge.json")
        val hub = BridgeHub(rendezvous, if (softwareRendering) Rendering.SOFTWARE else Rendering.GPU)

        hub.start()

        // The proxy's own hook kills the clients; this one removes the rendezvous file they dial through.
        Runtime.getRuntime().addShutdownHook(Thread(hub::close, "mcp-bridge-shutdown"))
        val manager = SessionManager(launcher, service.settingsStore, hub)
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

    private companion object {
        private val logger = InlineLogger()
    }
}

public fun main(args: Array<String>): Unit = McpCommand().main(args)
