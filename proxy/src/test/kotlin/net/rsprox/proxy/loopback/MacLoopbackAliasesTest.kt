package net.rsprox.proxy.loopback

import net.rsprox.proxy.util.OperatingSystem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MacLoopbackAliasesTest {
    @Test
    fun `session helper reconciles loopback aliases and cleans them up`() {
        val script = MacLoopbackAliases.sessionHelperScript()
        assertTrue(script.startsWith("#!/bin/bash\n"), "helper must start with a shebang, got:\n$script")
        // It only ever touches lo0 loopback aliases, both directions.
        assertTrue(script.contains("/sbin/ifconfig lo0 alias"))
        assertTrue(script.contains("/sbin/ifconfig lo0 -alias"))
        // 127/8 is validated inside the helper so it can never touch a non-loopback address.
        assertTrue(script.contains("^127\\."))
        // It watches the parent pid and removes everything on exit (no standing state).
        assertTrue(script.contains("kill -0"))
        assertTrue(script.contains("trap cleanup EXIT"))
    }

    @Test
    fun `aliasing is only supported on macOS`() {
        assertTrue(MacLoopbackAliases.isSupported(OperatingSystem.MAC))
        assertFalse(MacLoopbackAliases.isSupported(OperatingSystem.WINDOWS))
        assertFalse(MacLoopbackAliases.isSupported(OperatingSystem.UNIX))
    }

    @Test
    fun `parses netstat output for connecting and established world addresses`() {
        val output =
            """
            Active Internet connections (including servers)
            Proto Recv-Q Send-Q  Local Address          Foreign Address        (state)
            tcp4       0      0  127.0.0.1.49852        127.1.44.2.43701       SYN_SENT
            tcp4       0      0  127.0.0.1.49860        127.1.45.2.43701       ESTABLISHED
            tcp4       0      0  127.1.45.2.43701       127.0.0.1.49860        ESTABLISHED
            tcp4       0      0  192.168.1.5.50122      140.82.121.4.443       ESTABLISHED
            tcp6       0      0  ::1.7000               ::1.49999              ESTABLISHED
            """.trimIndent()
        val parsed = MacLoopbackAliases.parseActiveWorldConnections(output, setOf(43701))
        // SYN_SENT world is flagged as connecting (to be aliased on demand).
        assertTrue("127.1.44.2" in parsed.connecting)
        // Both the connecting and the established world appear in the live set.
        assertTrue("127.1.44.2" in parsed.all)
        assertTrue("127.1.45.2" in parsed.all)
        // The established world is not (re-)connecting.
        assertFalse("127.1.45.2" in parsed.connecting)
        // Non-loopback / IPv6 foreign addresses are ignored.
        assertFalse(parsed.all.any { it.startsWith("140.") || it.startsWith("192.") })
    }

    @Test
    fun `only counts connections to the proxy port and handles private-server suffixes`() {
        // A private-server target uses a different last octet (.3 here); a connection to
        // an unrelated loopback port must be ignored, while the proxy-port one is caught.
        val output =
            """
            tcp4       0      0  127.0.0.1.50001        127.2.30.3.43702       SYN_SENT
            tcp4       0      0  127.0.0.1.50002        127.9.9.9.6379         ESTABLISHED
            """.trimIndent()
        val parsed = MacLoopbackAliases.parseActiveWorldConnections(output, setOf(43702))
        // The private-server world reached on the proxy port is detected.
        assertTrue("127.2.30.3" in parsed.connecting)
        // The unrelated loopback connection (e.g. redis on 6379) is ignored.
        assertFalse("127.9.9.9" in parsed.all)
    }

    @Test
    fun `eviction drops idle worlds past the grace period but never active ones`() {
        val now = 100_000L
        val grace = 10_000L
        val desired =
            mapOf(
                "127.1.1.2" to now - 20_000L, // idle and expired -> remove
                "127.1.2.2" to now - 2_000L, // idle but within grace -> keep
                "127.1.3.2" to now - 60_000L, // very stale but currently active -> keep
            )
        val active = setOf("127.1.3.2")
        val removed = MacLoopbackAliases.computeRemovals(desired, active, now, maxAliases = 8, graceMillis = grace)
        assertTrue("127.1.1.2" in removed)
        assertFalse("127.1.2.2" in removed)
        assertFalse("127.1.3.2" in removed)
    }

    @Test
    fun `hard cap trims the oldest idle worlds while keeping active ones`() {
        val now = 100_000L
        // 6 worlds, cap of 3. Two are active; the four idle ones are within grace.
        val desired =
            mapOf(
                "127.0.0.2" to now - 5_000L, // active
                "127.0.0.3" to now - 4_000L, // active
                "127.0.0.4" to now - 9_000L, // idle, oldest
                "127.0.0.5" to now - 8_000L, // idle
                "127.0.0.6" to now - 7_000L, // idle
                "127.0.0.7" to now - 1_000L, // idle, newest
            )
        val active = setOf("127.0.0.2", "127.0.0.3")
        val removed = MacLoopbackAliases.computeRemovals(desired, active, now, maxAliases = 3, graceMillis = 60_000L)
        // 6 -> 3 means dropping the 3 oldest idle; the newest idle and both active survive.
        assertEquals(setOf("127.0.0.4", "127.0.0.5", "127.0.0.6"), removed)
        assertFalse("127.0.0.2" in removed)
        assertFalse("127.0.0.3" in removed)
        assertFalse("127.0.0.7" in removed)
    }
}
