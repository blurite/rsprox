package net.rsprox.proxy.loopback

import java.nio.file.Files
import java.util.concurrent.TimeUnit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.io.path.deleteIfExists
import kotlin.io.path.writeText

class MacLoopbackAliasesTest {
    @Test
    fun `detects pending world alias for a proxy port`() {
        val output =
            """
            Active Internet connections (including servers)
            Proto Recv-Q Send-Q  Local Address          Foreign Address        (state)
            tcp4       0      0  127.0.0.1.49852        127.1.44.2.43701       SYN_SENT
            tcp4       0      0  127.0.0.1.49860        127.1.45.2.43701       ESTABLISHED
            tcp4       0      0  192.168.1.5.50122      140.82.121.4.443       ESTABLISHED
            tcp6       0      0  ::1.7000               ::1.49999              ESTABLISHED
            """.trimIndent()

        val active = MacLoopbackAliases.parseActiveWorldConnections(output, setOf(43701))

        assertEquals(setOf("127.1.44.2", "127.1.45.2"), active.all)
        assertEquals(setOf("127.1.44.2"), active.connecting)
    }

    @Test
    fun `expires idle aliases but retains live aliases`() {
        val now = 100_000L
        val current =
            mapOf(
                "127.1.44.2" to 80_000L,
                "127.1.45.2" to 95_000L,
                "127.1.46.2" to 80_000L,
            )
        val active =
            MacLoopbackAliases.ActiveWorldConnections(
                all = setOf("127.1.46.2"),
                connecting = emptySet(),
            )

        val next = MacLoopbackAliases.nextDesiredAliases(current, active, now)

        assertEquals(
            mapOf(
                "127.1.45.2" to 95_000L,
                "127.1.46.2" to now,
            ),
            next,
        )
    }

    @Test
    fun `never requests more than eight aliases`() {
        val current = (1..7).associate { "127.1.$it.2" to 100_000L }
        val active =
            MacLoopbackAliases.ActiveWorldConnections(
                all = current.keys,
                connecting = setOf("127.1.8.2", "127.1.9.2"),
            )

        val next = MacLoopbackAliases.nextDesiredAliases(current, active, 100_001L)

        assertEquals(8, next.size)
        assertEquals(current.keys, next.keys.intersect(current.keys))
        assertEquals(1, next.keys.count { it == "127.1.8.2" || it == "127.1.9.2" })
    }

    @Test
    fun `privileged helper rejects unsafe addresses and enforces its own cap`() {
        if (!System.getProperty("os.name").startsWith("Mac")) return
        if (ProcessBuilder("/usr/bin/id", "-u").start().inputStream.bufferedReader().readText().trim() == "0") return
        val desiredFile = Files.createTempFile("rsprox-loopback-test", ".txt")
        val traceFile = Files.createTempFile("rsprox-loopback-trace", ".txt")
        try {
            desiredFile.writeText(
                buildString {
                    appendLine("8.8.8.8")
                    appendLine("127.0.0.1")
                    for (id in 1..9) appendLine("127.1.$id.2")
                },
            )
            val process =
                ProcessBuilder(
                    "/bin/bash",
                    "-x",
                    "-c",
                    MacLoopbackAliases.sessionHelperScript(),
                    "rsprox-loopback-helper",
                    ProcessHandle.current().pid().toString(),
                    desiredFile.toString(),
                ).redirectErrorStream(true).redirectOutput(traceFile.toFile()).start()
            Thread.sleep(700)
            process.destroy()
            if (!process.waitFor(2, TimeUnit.SECONDS)) process.destroyForcibly()
            val trace = traceFile.toFile().readText()
            val aliasAttempts = trace.lineSequence().filter { "/sbin/ifconfig lo0 alias" in it }.toList()
            val attemptedAddresses = aliasAttempts.map { it.substringAfter("lo0 alias ") }.toSet()

            assertEquals(8, attemptedAddresses.size)
            assertEquals(false, aliasAttempts.any { "8.8.8.8" in it || "127.0.0.1" in it })
        } finally {
            desiredFile.deleteIfExists()
            traceFile.deleteIfExists()
        }
    }
}
