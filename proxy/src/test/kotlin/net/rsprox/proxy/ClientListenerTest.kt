package net.rsprox.proxy

import io.netty.buffer.ByteBufAllocator
import net.rsprox.proxy.binary.BinaryHeader
import net.rsprox.proxy.target.ProxyTargetConfig
import net.rsprox.shared.SessionMonitor
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

object WaitsForInput {
    @JvmStatic
    fun main(args: Array<String>) {
        System.`in`.read()
    }
}

class ClientListenerTest {
    private class ClosedPorts(
        private val failing: Boolean = false,
    ) : ClientListener {
        val closed = ArrayList<Int>()

        override fun onClientLaunch(
            port: Int,
            target: ProxyTargetConfig,
        ): SessionMonitor<BinaryHeader>? = null

        override fun onClientClosed(port: Int) {
            closed += port
            if (failing) throw IllegalStateException("cannot close")
        }
    }

    private fun waiting(): Process {
        val java = ProcessHandle.current().info().command().get()

        return ProcessBuilder(java, "-cp", System.getProperty("java.class.path"), WaitsForInput::class.java.name).start()
    }

    @Test
    fun `killing the client on a port tells every listener that it closed, past one that throws`() {
        val service = ProxyService(ByteBufAllocator.DEFAULT)
        val failing = ClosedPorts(failing = true)
        val later = ClosedPorts()
        service.addClientListener(failing)
        service.addClientListener(later)

        service.killAliveProcess(43701)

        assertEquals(listOf(43701), failing.closed)
        assertEquals(listOf(43701), later.closed)
    }

    @Test
    fun `the exit of a process is reported once the last of the watched processes has exited`() {
        val first = waiting()
        val second = waiting()
        val exited = CompletableFuture<Unit>()

        try {
            assertTrue(whenAllExit(listOf(first.toHandle(), second.toHandle())) { exited.complete(Unit) })

            first.outputStream.close()
            first.waitFor(20, TimeUnit.SECONDS)
            assertFailsWith<TimeoutException> { exited.get(300, TimeUnit.MILLISECONDS) }

            second.outputStream.close()
            exited.get(20, TimeUnit.SECONDS)
        } finally {
            first.destroyForcibly()
            second.destroyForcibly()
        }
    }

    @Test
    fun `processes that have all exited are not watched`() {
        val process = waiting()
        process.outputStream.close()
        process.waitFor(20, TimeUnit.SECONDS)

        assertFalse(whenAllExit(listOf(process.toHandle())) { error("nothing is left to exit") })
    }
}
