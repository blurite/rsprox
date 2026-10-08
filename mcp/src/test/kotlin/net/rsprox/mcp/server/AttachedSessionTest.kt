package net.rsprox.mcp.server

import com.fasterxml.jackson.databind.JsonNode
import net.rsprox.mcp.bridge.TestHub
import net.rsprox.mcp.packets.TapSettingSetStore
import net.rsprox.mcp.session.FakeLauncher
import net.rsprox.mcp.session.SessionManager
import net.rsprox.mcp.session.target
import net.rsprox.proxy.binary.BinaryHeader
import net.rsprox.shared.SessionMonitor
import net.rsprox.shared.StreamDirection
import net.rsprox.shared.property.ChildProperty
import net.rsprox.shared.property.RootProperty
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AttachedSessionTest {
    private val launcher = FakeLauncher()
    private val fixture = TestHub()
    private val manager = SessionManager(launcher, TapSettingSetStore, fixture.hub)
    private val server = McpHttpServer(0, tools { manager }, "1.2.3").also { it.start() }
    private val http = HttpClient.newHttpClient()

    private val header =
        BinaryHeader(
            headerVersion = 1,
            revision = 235,
            subRevision = 1,
            clientType = 1,
            platformType = 1,
            timestamp = 1_700_000_000_000,
            worldId = 301,
            worldFlags = 0,
            worldLocation = 0,
            worldHost = "127.0.1.3",
            worldActivity = "",
            localPlayerIndex = 7,
            accountHash = ByteArray(0),
            clientName = "RuneLite",
            js5MasterIndex = ByteArray(0),
        )

    @AfterTest
    fun cleanUp() {
        server.close()
        fixture.close()
    }

    private fun toolResult(
        tool: String,
        arguments: String,
    ): JsonNode {
        val body =
            """{"jsonrpc":"2.0","id":1,"method":"tools/call","params":{"name":"$tool","arguments":$arguments}}"""

        val request =
            HttpRequest
                .newBuilder(URI("http://127.0.0.1:${server.localPort}/mcp"))
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build()

        val response = http.send(request, HttpResponse.BodyHandlers.ofString())

        return McpDispatcher.MAPPER.readTree(response.body()).get("result")
    }

    private fun call(
        tool: String,
        arguments: String = "{}",
    ): String {
        val result = toolResult(tool, arguments)
        val text = result.get("content").single().get("text").asText()
        assertEquals(false, result.get("isError").asBoolean(), text)

        return text
    }

    private fun error(
        tool: String,
        arguments: String,
    ): String {
        val result = toolResult(tool, arguments)
        val text = result.get("content").single().get("text").asText()
        assertEquals(true, result.get("isError").asBoolean(), text)

        return text
    }

    private fun sessions(): List<String> =
        McpDispatcher.MAPPER
            .readTree(call("session_list"))
            .get("sessions")
            .map { it.toString() }

    private fun attach(
        proxyPort: Int = 43701,
        captureFolder: String? = "Local",
    ): SessionMonitor<BinaryHeader> =
        checkNotNull(manager.attach(proxyPort, target(1, "Local").copy(binaryFolder = captureFolder)))

    private fun SessionMonitor<BinaryHeader>.logIn(): SessionMonitor<BinaryHeader> {
        val login = forSession(header)
        login.onLogin(header)
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

    private fun rows(arguments: String): List<String> = call("packets_read", arguments).lines().drop(1)

    @Test
    fun `a client the GUI launches is listed as an attached session, numbered with the launched ones`() {
        call("session_start", """{"target":"My Server","wait_ms":0}""")

        attach(proxyPort = 43701)

        assertEquals(
            listOf(
                """{"session":"s1","kind":"launched","target":"My Server","state":"launching","generation":1,""" +
                    """"proxyPort":43751,"httpPort":43650,"cursor":1}""",
                """{"session":"s2","kind":"attached","target":"Local","state":"attached",""" +
                    """"proxyPort":43701,"cursor":1}""",
            ),
            sessions(),
        )
    }

    @Test
    fun `a client that the MCP server launched itself gets no attached session`() {
        call("session_start", """{"target":"My Server","wait_ms":0}""")

        assertNull(manager.attach(FakeLauncher.FIRST_PROXY_PORT, target(1, "My Server")))

        assertEquals(listOf("s1"), manager.list().map { it.session })
    }

    @Test
    fun `the session list reports each fact of a login from the moment the proxy knows it`() {
        val client = attach()
        val attached = """{"session":"s1","kind":"attached","target":"Local","state":"attached","proxyPort":43701"""
        val accepted =
            """"login":{"epoch":1,"revision":235,"world":301,"host":"127.0.1.3","localPlayerIndex":7,""" +
                """"connectedAt":"2023-11-14T22:13:20Z""""

        assertEquals(listOf("""$attached,"cursor":1}"""), sessions())

        val login = client.forSession(header)
        login.onLogin(header)

        assertEquals(listOf("""$attached,$accepted,"online":true,"transcribing":false},"cursor":2}"""), sessions())

        login.onCacheUpdate { error("the test formats no packet that needs the cache") }
        val recorded = """$accepted,"captureFile":"Local/${header.fileName()}""""

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

        login.onLogout(header)

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
                """{"session":"s1","kind":"attached","target":"Local","state":"attached","proxyPort":43701,""" +
                    """"login":{"epoch":1,"revision":235,"world":301,"host":"127.0.1.3","localPlayerIndex":7,""" +
                    """"connectedAt":"2023-11-14T22:13:20Z","online":true,"transcribing":false},"cursor":2}""",
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
        first.onLogout(header)
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
            call("packets_read", """{"session":"s1"}""").lines(),
        )

        assertEquals(listOf("4 L1 T5 C NO_TIMEOUT [no_timeout] "), rows("""{"after":3,"origin":"client"}"""))
    }

    @Test
    fun `session_stop and session_start refuse an attached session and leave its client alone`() {
        attach()
        val refusal =
            "is not available for an attached session: session s1 belongs to a client that was launched " +
                "from the rsprox GUI, so only its packets can be read"

        assertEquals("session_stop $refusal", error("session_stop", """{"session":"s1"}"""))
        assertEquals("session_start $refusal", error("session_start", """{"session":"s1","wait_ms":0}"""))

        assertEquals(emptyList(), launcher.killed)
        assertEquals(emptyList(), launcher.reserved)
        assertEquals("attached", manager.list().single().state)
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

        val clientTools = tools { manager }.map { it.name }.filter { it.startsWith("client_") }

        assertEquals(10, clientTools.size)

        for (tool in clientTools) {
            assertEquals(
                "$tool is not available for an attached session: session s1 belongs to a client that was " +
                    "launched from the rsprox GUI, so only its packets can be read",
                error(tool, required[tool] ?: "{}"),
            )
        }
    }

    @Test
    fun `an attached session ends when its client is closed and stays listed with its packets`() {
        attach(proxyPort = 43701).logIn()
        attach(proxyPort = 43702)

        manager.detach(43701)
        manager.detach(43701)
        manager.detach(43999)

        assertEquals(
            listOf("ended" to "the client was closed", "attached" to null),
            manager.list().map { it.state to it.reason },
        )

        assertEquals(
            listOf(
                "1 L0 T0 P CLIENT_ATTACHED proxyPort=43701",
                "2 L1 T0 P LOGIN revision=235 world=301",
                "3 L1 T0 P CLIENT_EXITED the client was closed",
            ),
            rows("""{"session":"s1"}"""),
        )
    }
}
