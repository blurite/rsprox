package net.rsprox.mcp

import com.github.michaelbull.logging.InlineLogger
import net.rsprox.mcp.bridge.Gate
import net.rsprox.mcp.bridge.Rendering
import net.rsprox.mcp.server.McpHttpServer
import net.rsprox.mcp.server.ToolError
import net.rsprox.mcp.server.tools
import net.rsprox.mcp.session.SessionManager
import net.rsprox.proxy.ProxyExtension
import net.rsprox.proxy.ProxyService
import java.io.IOException
import java.util.concurrent.atomic.AtomicReference

/**
 * Serves the MCP endpoint from inside the rsprox GUI, which finds this class through `META-INF/services`.
 * Every client that is launched from the GUI while the sessions exist becomes an attached session. The
 * proxy keeps the filter and setting stores of the GUI, so the packet logs hold what the GUI shows.
 */
public class McpExtension : ProxyExtension {
    /** The switch of the endpoint, or null before [start]. Set on the main thread, read on the Swing thread. */
    @Volatile
    private var switch: EndpointSwitch? = null

    /** Serve the endpoint on the configured port, if the user turned it on and the port is free. */
    override fun start(service: ProxyService) {
        val gate = Gate()
        val switch =
            EndpointSwitch(service.getMcpPort(), gate) { opened ->
                sessionManager(service, sideloadDir = null, Rendering.GPU, service.getMcpPluginEnabled(), opened, gate)
            }

        this.switch = switch
        switch.atStartUp(service.getMcpEnabled())
    }

    /** Serve the endpoint from this call on. Returns null once it is served, and otherwise why it is not. */
    override fun serve(): String? = checkNotNull(switch) { "the extension has not been started" }.serve()

    /** Stop serving the endpoint from this call on. */
    override fun stopServing() {
        switch?.stopServing()
    }
}

/**
 * Turns the endpoint of the GUI on and off while the GUI runs.
 *
 * On binds [port] and serves the sessions over HTTP. The first time, it also builds the sessions, with
 * the bridge hub and the listener that attaches a session to every client the proxy launches from then
 * on. A client that was already open is not attached, because the proxy asks its listeners only at a
 * launch.
 *
 * Off closes the port and every connection, and closes [gate], so that a request that still runs can no
 * longer fork a client, kill one or send one a request. Off does not take the sessions down. They, the
 * hub and the listener stay in memory until the GUI exits, and the packet taps on the open clients keep
 * recording, for three reasons. The proxy cannot take a packet tap off a client that runs, or a listener
 * off itself. Closing the hub closes every link, and a closed link has the client behind it killed, so
 * turning the endpoint off would close the clients it launched. And a tap that dropped packets while
 * the endpoint is off would leave a session that lies when it is listed again: its login state and its
 * tick would be stale and its log would have a hole. Nothing outside the process reaches what is kept,
 * since the port is its only door. The GUI holds the same packets either way.
 *
 * On again serves the same sessions on a new HTTP server, so they are listed with their logs and cursors.
 */
internal class EndpointSwitch(
    /** The loopback port of the endpoint. */
    private val port: Int,
    /** The gate that the sessions act on clients through. It is open exactly while the endpoint is served. */
    private val gate: Gate,
    /**
     * Builds the sessions, the first time the endpoint is served. It may add what must be closed when a
     * later step fails to the list it is given.
     */
    private val build: (opened: MutableList<AutoCloseable>) -> SessionManager,
) {
    /** The sessions, or null until the endpoint has been served once. They outlive every HTTP server. */
    private val sessions = AtomicReference<SessionManager?>()

    /** The HTTP server, or null while the endpoint is off. Guarded by the monitor of the switch. */
    private var http: McpHttpServer? = null

    init {
        gate.close()
    }

    /** Serve the endpoint if [enabled] says the user turned it on, and log why it is not served otherwise. */
    fun atStartUp(enabled: Boolean) {
        if (!enabled) {
            logger.info {
                "The MCP endpoint is off. Turn it on with File > Serve MCP Endpoint, or with mcp.enabled in " +
                    "proxy.properties."
            }

            return
        }

        serve()?.let { refusal -> logger.warn { refusal } }
    }

    /**
     * Serve the endpoint from this call on. Returns null once it is served, and otherwise the reason it
     * is not, in words for the user: the port is taken, or the sessions cannot be built. Neither leaves
     * anything bound, and neither is thrown, since the endpoint is no reason for its host to stop.
     * Idempotent. Safe to call from any thread, and quick enough for the Swing thread.
     */
    @Synchronized
    fun serve(): String? {
        if (http != null) return null

        val server = endpoint(port, sessions)

        try {
            server.start()
        } catch (e: IOException) {
            return "The MCP endpoint is not served: 127.0.0.1:$port cannot be bound (${e.message}). Another " +
                "rsprox or the standalone MCP server may hold it. Close that one, or set mcp.port in " +
                "proxy.properties."
        }

        if (sessions.get() == null) {
            try {
                sessions.set(
                    closingOnFailure { opened ->
                        opened += server
                        build(opened)
                    },
                )
            } catch (e: Exception) {
                logger.error(e) { "The MCP endpoint could not start and is not served" }

                return "The MCP endpoint is not served: it could not start (${e.message}). The log has the details."
            }
        }

        gate.open()
        http = server
        logger.info { "MCP endpoint listening on http://127.0.0.1:$port/mcp" }

        return null
    }

    /**
     * Stop serving from this call on: no request that still runs acts on a client once this returns, and
     * no connection is accepted or answered. The sessions are kept, as the class says. Idempotent. Safe
     * to call from any thread, and quick enough for the Swing thread.
     */
    @Synchronized
    fun stopServing() {
        val server = http ?: return

        http = null
        gate.close()
        server.close()
        logger.info { "The MCP endpoint is off. Its sessions are kept until rsprox exits." }
    }

    private companion object {
        /** The logger of the endpoint. */
        private val logger = InlineLogger()
    }
}

/** Build the endpoint on [port], whose tools refuse every call until [sessions] holds the sessions. */
internal fun endpoint(
    port: Int,
    sessions: AtomicReference<SessionManager?>,
): McpHttpServer =
    McpHttpServer(
        port,
        tools { sessions.get() ?: throw ToolError("rsprox is still starting; try again shortly") },
        System.getenv("APP_VERSION") ?: "dev",
    )
