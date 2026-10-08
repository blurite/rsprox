package net.rsprox.mcp.server

import net.rsprox.mcp.session.FakeLauncher
import net.rsprox.mcp.session.loginHeader
import net.rsprox.mcp.session.target
import net.rsprox.proxy.binary.BinaryHeader
import net.rsprox.shared.SessionMonitor
import net.rsprox.shared.StreamDirection
import net.rsprox.shared.property.ChildProperty
import net.rsprox.shared.property.RootProperty
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AttachedSessionTest {
    private val server = ToolServer()

    @AfterTest
    fun cleanUp() {
        server.close()
    }

    private fun sessions(): List<String> = server.callJson("session_list").get("sessions").map { it.toString() }

    private fun attach(
        proxyPort: Int = 43701,
        captureFolder: String? = "Local",
    ): SessionMonitor<BinaryHeader> =
        checkNotNull(server.manager.attach(proxyPort, target(1, "Local").copy(binaryFolder = captureFolder)))

    private fun SessionMonitor<BinaryHeader>.logIn(): SessionMonitor<BinaryHeader> {
        val login = forSession(loginHeader)
        login.onLogin(loginHeader)
        login.onCacheUpdate { error("the test formats no packet that needs the cache") }

        return login
    }

    private fun SessionMonitor<BinaryHeader>.packet(
        tick: Int,
        direction: StreamDirection,
        prot: String,
    ) {
        onPacketDirection(direction)
        onTranscribe(
            tick,
            object : RootProperty {
                override val prot = prot
                override val children = mutableListOf<ChildProperty<*>>()
            },
        )
    }

    @Test
    fun `a client the GUI launches is listed as an attached session, numbered with the launched ones`() {
        server.call("session_start", """{"target":"My Server","wait_ms":0}""")

        attach(proxyPort = 43701)

        assertEquals(
            listOf(
                """{"session":"s1","kind":"launched","target":"My Server","access":"drive","state":"launching",""" +
                    """"generation":1,"proxyPort":43751,"httpPort":43650,"cursor":1}""",
                """{"session":"s2","kind":"attached","target":"Local","access":"read","state":"attached",""" +
                    """"proxyPort":43701,"cursor":1}""",
            ),
            sessions(),
        )
    }

    @Test
    fun `a client that the MCP server launched itself gets no attached session`() {
        server.call("session_start", """{"target":"My Server","wait_ms":0}""")

        assertNull(server.manager.attach(FakeLauncher.FIRST_PROXY_PORT, target(1, "My Server")))

        assertEquals(listOf("s1"), server.manager.list().map { it.session })
    }

    @Test
    fun `the session list reports each fact of a login from the moment the proxy knows it`() {
        val client = attach()
        val attached =
            """{"session":"s1","kind":"attached","target":"Local","access":"read","state":"attached",""" +
                """"proxyPort":43701"""
        val accepted =
            """"login":{"epoch":1,"revision":235,"world":301,"host":"127.0.1.3","localPlayerIndex":7,""" +
                """"connectedAt":"2023-11-14T22:13:20Z""""

        assertEquals(listOf("""$attached,"cursor":1}"""), sessions())

        val login = client.forSession(loginHeader)
        login.onLogin(loginHeader)

        val recorded = """$accepted,"captureFile":"Local/${loginHeader.fileName()}""""

        assertEquals(listOf("""$attached,$recorded,"online":true,"transcribing":false},"cursor":2}"""), sessions())

        login.packet(tick = 4, StreamDirection.SERVER_TO_CLIENT, "REBUILD_NORMAL")

        assertEquals(
            listOf("""$attached,$recorded,"online":true,"transcribing":true,"tick":4},"cursor":3}"""),
            sessions(),
        )

        login.onNameUpdate("Alice")
        login.packet(tick = 5, StreamDirection.CLIENT_TO_SERVER, "NO_TIMEOUT")

        assertEquals(
            listOf("""$attached,$recorded,"name":"Alice","online":true,"transcribing":true,"tick":5},"cursor":4}"""),
            sessions(),
        )

        login.onLogout(loginHeader)

        assertEquals(
            listOf("""$attached,$recorded,"name":"Alice","online":false,"transcribing":true,"tick":5},"cursor":5}"""),
            sessions(),
        )
    }

    @Test
    fun `a login of a target that is not recorded has no capture file`() {
        attach(captureFolder = null).logIn()

        assertEquals(
            listOf(
                """{"session":"s1","kind":"attached","target":"Local","access":"read","state":"attached",""" +
                    """"proxyPort":43701,"login":{"epoch":1,"revision":235,"world":301,"host":"127.0.1.3",""" +
                    """"localPlayerIndex":7,"connectedAt":"2023-11-14T22:13:20Z","online":true,""" +
                    """"transcribing":false},"cursor":2}""",
            ),
            sessions(),
        )
    }

    @Test
    fun `the packets of an attached session are read by cursor, with a new login number for each login`() {
        val client = attach()
        val first = client.logIn()
        first.packet(tick = 4, StreamDirection.SERVER_TO_CLIENT, "REBUILD_NORMAL")
        first.packet(tick = 5, StreamDirection.CLIENT_TO_SERVER, "NO_TIMEOUT")
        first.onLogout(loginHeader)
        client.logIn().packet(tick = 1, StreamDirection.SERVER_TO_CLIENT, "REBUILD_NORMAL")

        assertEquals(
            listOf(
                """{"next":7,"head":7,"dropped":0,"timedOut":false,"count":7}""",
                "1 L0 T0 P CLIENT_ATTACHED proxyPort=43701",
                "2 L1 T0 P LOGIN revision=235 world=301",
                "3 L1 T4 S REBUILD_NORMAL [rebuild_normal] ",
                "4 L1 T5 C NO_TIMEOUT [no_timeout] ",
                "5 L1 T0 P LOGOUT world=301",
                "6 L2 T0 P LOGIN revision=235 world=301",
                "7 L2 T1 S REBUILD_NORMAL [rebuild_normal] ",
            ),
            server.call("packets_read", """{"session":"s1"}""").lines(),
        )

        assertEquals(listOf("4 L1 T5 C NO_TIMEOUT [no_timeout] "), server.rows("""{"after":3,"origin":"client"}"""))
    }

    @Test
    fun `the monitor of a login is its own monitor for that login`() {
        val login = attach().logIn()

        login.forSession(loginHeader).packet(tick = 4, StreamDirection.SERVER_TO_CLIENT, "REBUILD_NORMAL")

        assertEquals(listOf("3 L1 T4 S REBUILD_NORMAL [rebuild_normal] "), server.rows("""{"after":2}"""))
    }

    @Test
    fun `session_stop and session_start refuse an attached session and leave its client alone`() {
        attach()
        val refusal =
            "is not available for an attached session: session s1 belongs to a client that was launched " +
                "from the rsprox GUI, so only its packets can be read"

        assertEquals("session_stop $refusal", server.error("session_stop", """{"session":"s1"}"""))
        assertEquals("session_start $refusal", server.error("session_start", """{"session":"s1","wait_ms":0}"""))

        assertEquals(emptyList(), server.launcher.killed)
        assertEquals(emptyList(), server.launcher.reserved)
        assertEquals("attached", server.manager.list().single().state)
    }

    @Test
    fun `every client tool refuses an attached session`() {
        attach()
        val required =
            mapOf(
                "client_login" to """{"username":"alice","password":"any"}""",
                "client_type" to """{"text":"hello"}""",
                "client_interact" to """{"target":"tile","x":3094,"y":3107}""",
            )

        val clientTools = tools { server.manager }.map { it.name }.filter { it.startsWith("client_") }

        assertEquals(10, clientTools.size)

        for (tool in clientTools) {
            assertEquals(
                "$tool is not available for an attached session: session s1 belongs to a client that was " +
                    "launched from the rsprox GUI, so only its packets can be read",
                server.error(tool, required[tool] ?: "{}"),
            )
        }
    }

    @Test
    fun `an attached session ends when its client is closed and stays listed with its packets`() {
        attach(proxyPort = 43701).logIn()
        attach(proxyPort = 43702)

        server.manager.detach(43701)
        server.manager.detach(43701)
        server.manager.detach(43999)

        assertEquals(
            listOf("ended" to "the client was closed", "attached" to null),
            server.manager.list().map { it.state to it.reason },
        )

        assertEquals(
            listOf(
                "1 L0 T0 P CLIENT_ATTACHED proxyPort=43701",
                "2 L1 T0 P LOGIN revision=235 world=301",
                "3 L1 T0 P CLIENT_EXITED the client was closed",
            ),
            server.rows("""{"session":"s1"}"""),
        )
    }
}
