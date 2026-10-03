package net.rsprox.mcp.session

import java.net.ServerSocket
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ProxyServiceLauncherTest {
    @Test
    fun `a proxy port must never be handed out while it or its http port is taken`() {
        val allocated = generateSequence(100) { it + 1 }.iterator()
        val taken = setOf(100, 1101)

        val port = firstFreePort(allocated::next, { it + 1000 }, { it !in taken })

        assertEquals(102, port)
        assertEquals(103, allocated.next())
    }

    @Test
    fun `a port that another socket holds cannot be bound and a released one can`() {
        val holder = ServerSocket(0)
        val port = holder.localPort

        holder.use { assertFalse(canBind(port)) }

        assertTrue(canBind(port))
    }
}
