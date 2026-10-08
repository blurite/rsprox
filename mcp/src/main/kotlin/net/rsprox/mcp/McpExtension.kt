package net.rsprox.mcp

import com.github.michaelbull.logging.InlineLogger
import net.rsprox.mcp.bridge.Rendering
import net.rsprox.mcp.server.McpHttpServer
import net.rsprox.mcp.server.ToolError
import net.rsprox.mcp.server.tools
import net.rsprox.mcp.session.SessionManager
import net.rsprox.proxy.ClientListener
import net.rsprox.proxy.ProxyExtension
import net.rsprox.proxy.ProxyService
import net.rsprox.proxy.binary.BinaryHeader
import net.rsprox.proxy.target.ProxyTargetConfig
import net.rsprox.shared.SessionMonitor
import java.io.IOException
import java.util.concurrent.atomic.AtomicReference

/**
 * Serves the MCP endpoint from inside the rsprox GUI, which finds this class through `META-INF/services`.
 * Every client that is launched from the GUI becomes an attached session. The proxy keeps the filter and
 * setting stores of the GUI, so the packet logs hold what the GUI shows.
 */
public class McpExtension : ProxyExtension {
    /** Serve the endpoint on the configured port, unless it is turned off or the port is taken. */
    override fun start(service: ProxyService) {
        if (!service.getMcpEnabled()) {
            logger.info { "The MCP endpoint is turned off by mcp.enabled in proxy.properties" }

            return
        }

        serveOrNull(service.getMcpPort()) { opened ->
            val manager = sessionManager(service, sideloadDir = null, Rendering.GPU, opened)

            service.addClientListener(AttachingListener(manager))

            manager
        }
    }

    private companion object {
        /** The logger of the extension. */
        private val logger = InlineLogger()
    }
}

/** Attaches a session to each client the proxy launches, and ends it when the client is gone. */
private class AttachingListener(
    /** The sessions that the clients are attached to. */
    private val sessions: SessionManager,
) : ClientListener {
    /** Attach a session to the client, unless the session manager launched it itself. */
    override fun onClientLaunch(
        port: Int,
        target: ProxyTargetConfig,
    ): SessionMonitor<BinaryHeader>? = sessions.attach(port, target)

    /** End the session that is attached to the client, if any. */
    override fun onClientClosed(port: Int) {
        sessions.detach(port)
    }
}

/**
 * Bind the endpoint to [port] and then build the sessions it serves, which may add what must be closed
 * on a failure to the list they are given. Returns null when the port is taken or the sessions cannot be
 * built. Either is logged and neither is thrown, since the endpoint is no reason for its host to stop.
 */
internal fun serveOrNull(
    port: Int,
    sessions: (opened: MutableList<AutoCloseable>) -> SessionManager,
): McpHttpServer? {
    val built = AtomicReference<SessionManager?>()
    val http = endpoint(port, built)

    try {
        http.start()
    } catch (e: IOException) {
        endpointLogger.warn {
            "The MCP endpoint is not served: 127.0.0.1:$port cannot be bound (${e.message}). Another rsprox or " +
                "the standalone MCP server may hold it. Set mcp.port or mcp.enabled in proxy.properties."
        }

        return null
    }

    try {
        built.set(
            closingOnFailure { opened ->
                opened += http
                sessions(opened)
            },
        )
    } catch (e: Exception) {
        endpointLogger.error(e) { "The MCP endpoint could not start and is not served" }

        return null
    }

    endpointLogger.info { "MCP endpoint listening on http://127.0.0.1:$port/mcp" }

    return http
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

/** The logger of the endpoint. */
private val endpointLogger = InlineLogger()
