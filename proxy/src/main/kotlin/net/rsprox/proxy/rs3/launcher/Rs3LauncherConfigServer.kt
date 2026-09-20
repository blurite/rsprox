package net.rsprox.proxy.rs3.launcher

import com.sun.net.httpserver.HttpServer
import java.net.InetAddress
import java.net.InetSocketAddress
import java.util.UUID
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

/** One immutable configuration, served only on this instance's loopback address. No login credentials. */
internal class Rs3LauncherConfigServer(
    host: String,
) : AutoCloseable {
    private val address = InetAddress.getByName(host).also { require(it.isLoopbackAddress) }
    private val server = HttpServer.create(InetSocketAddress(address, 0), 4)
    private val executor =
        Executors.newSingleThreadExecutor { task ->
            Thread(task, "rs3-launcher-config").apply { isDaemon = true }
        }
    private val path = "/${UUID.randomUUID()}/jav_config.ws"
    private val closed = AtomicBoolean()
    val codebase: String = "http://$host:${server.address.port}/"
    val url: String = codebase + path.removePrefix("/")
    val requested = AtomicBoolean()

    fun start(body: ByteArray) {
        server.executor = executor
        server.createContext("/") { exchange ->
            try {
                if (exchange.requestURI.path != path) {
                    exchange.sendResponseHeaders(404, -1)
                } else if (exchange.requestMethod != "GET") {
                    exchange.sendResponseHeaders(405, -1)
                } else {
                    requested.set(true)
                    // Note: bootstrap 224.1 compares this header literally, including the charset spelling.
                    exchange.responseHeaders.set("Content-Type", "text/plain; charset=ISO-8859-1")
                    exchange.responseHeaders.set("Cache-Control", "no-store")
                    exchange.sendResponseHeaders(200, body.size.toLong())
                    exchange.responseBody.write(body)
                }
            } finally {
                exchange.close()
            }
        }
        server.start()
    }

    override fun close() {
        if (!closed.compareAndSet(false, true)) return
        try {
            server.stop(0)
        } finally {
            executor.shutdownNow()
        }
    }
}
