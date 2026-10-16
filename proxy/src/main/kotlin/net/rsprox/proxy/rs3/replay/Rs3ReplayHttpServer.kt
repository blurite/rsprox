package net.rsprox.proxy.rs3.replay

import com.sun.net.httpserver.HttpServer
import net.rsprox.cache.rs3.Rs3ReplayGroupStore
import net.rsprox.proxy.replay.ReplayAuthentication
import java.net.BindException
import java.net.InetAddress
import java.net.InetSocketAddress
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

/** Native 950's HTTP resource URL is /ms?m=0&a=...&k=...&g=...&c=...&v=... . */
internal class Rs3ReplayHttpServer private constructor(
    val host: String,
    private val cache: Rs3ReplayGroupStore,
    private val onFailure: (Throwable) -> Unit,
) : AutoCloseable {
    private val address = InetAddress.getByName(host).also { require(it.isLoopbackAddress) }
    private val server = HttpServer.create(InetSocketAddress(address, 80), 16)
    private val workers =
        Executors.newFixedThreadPool(4) { task ->
            Thread(task, "rs3-replay-http").apply { isDaemon = true }
        }
    private val closed = AtomicBoolean()
    val url: String = "http://$host:${server.address.port}"

    fun start() {
        server.executor = workers
        server.createContext("/") { exchange ->
            try {
                val authentication = ReplayAuthentication.response(exchange.requestURI.path)
                if (authentication != null && exchange.requestMethod in setOf("GET", "POST")) {
                    val bytes = authentication.toByteArray(Charsets.UTF_8)
                    exchange.responseHeaders.set("Content-Type", "application/json")
                    exchange.sendResponseHeaders(200, bytes.size.toLong())
                    exchange.responseBody.use { it.write(bytes) }
                    exchange.close()
                } else if (exchange.requestMethod != "GET" || exchange.requestURI.path != "/ms") {
                    exchange.sendResponseHeaders(404, -1)
                    exchange.close()
                } else {
                    val entries =
                        exchange.requestURI.rawQuery
                            .orEmpty()
                            .split('&')
                            .map { it.split('=', limit = 2) }
                    require(entries.all { it.size == 2 } && entries.map { it[0] }.distinct().size == entries.size)
                    val params = entries.associate { it[0] to it[1] }
                    require(params["m"] == "0")
                    val archive = checkNotNull(params["a"]?.toIntOrNull())
                    val group = checkNotNull(params["g"]?.toIntOrNull())
                    // Metadata comes from the recording, not client-supplied c/v values or a live cache.
                    cache.get(archive, group).whenComplete { bytes, error ->
                        try {
                            if (error != null) {
                                onFailure(error)
                                exchange.sendResponseHeaders(502, -1)
                            } else {
                                exchange.responseHeaders.set("Content-Type", "application/octet-stream")
                                exchange.sendResponseHeaders(200, bytes.size.toLong())
                                exchange.responseBody.write(bytes)
                            }
                        } catch (failure: Exception) {
                            if (!closed.get()) onFailure(failure)
                        } finally {
                            exchange.close()
                        }
                    }
                }
            } catch (error: Exception) {
                try {
                    exchange.sendResponseHeaders(400, -1)
                } finally {
                    exchange.close()
                }
            }
        }
        server.start()
    }

    override fun close() {
        if (!closed.compareAndSet(false, true)) return
        server.stop(0)
        workers.shutdownNow()
    }

    companion object {
        fun bind(
            world: Int,
            cache: Rs3ReplayGroupStore,
            onFailure: (Throwable) -> Unit,
        ): Rs3ReplayHttpServer {
            require(world in 0..65535)
            // NXT's resource host uses port 80, not the game ports or a URL authority.
            // Reserve a separate address per replay; OSRS replay addresses begin at .100.
            var lastFailure: BindException? = null
            for (slot in 125..149) {
                try {
                    return Rs3ReplayHttpServer("127.${world ushr 8}.${world and 255}.$slot", cache, onFailure)
                } catch (error: BindException) {
                    lastFailure = error
                }
            }
            throw IllegalStateException("No RS3 replay resource address available for world $world", lastFailure)
        }
    }
}
