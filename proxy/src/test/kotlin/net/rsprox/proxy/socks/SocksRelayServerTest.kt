package net.rsprox.proxy.socks

import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Proxy
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.CompletableFuture
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SocksRelayServerTest {
    private lateinit var relay: SocksRelayServer
    private lateinit var server: ServerSocket
    private lateinit var destination: CompletableFuture<InetSocketAddress?>

    @BeforeTest
    fun setUp() {
        relay = SocksRelayServer()
        server = ServerSocket(0, 50, InetAddress.getLoopbackAddress())
        destination = CompletableFuture()
        Thread {
            server.accept().use { accepted ->
                destination.complete(SocksRelayDestinations[accepted.port])
                val buffer = ByteArray(5)
                accepted.getInputStream().readNBytes(buffer, 0, buffer.size)
                accepted.getOutputStream().write(buffer)
            }
        }.apply { isDaemon = true }.start()
    }

    @AfterTest
    fun tearDown() {
        server.close()
        relay.close()
    }

    private fun socksSocket(): Socket {
        return Socket(Proxy(Proxy.Type.SOCKS, InetSocketAddress(InetAddress.getLoopbackAddress(), relay.port)))
    }

    private fun Socket.echo(): String {
        use {
            getOutputStream().write("hello".toByteArray())
            return String(getInputStream().readNBytes(5))
        }
    }

    @Test
    fun `relays an unaliased loopback address to the local server and records the requested address`() {
        val requested = InetSocketAddress(InetAddress.getByName("127.7.7.7"), server.localPort)

        val echoed = socksSocket().apply { connect(requested) }.echo()

        assertEquals("hello", echoed)
        assertEquals(requested, destination.get())
    }

    @Test
    fun `passes through a host name without recording a destination`() {
        val requested = InetSocketAddress.createUnresolved("localhost", server.localPort)

        val echoed = socksSocket().apply { connect(requested) }.echo()

        assertEquals("hello", echoed)
        assertEquals(null, destination.get())
    }
}
