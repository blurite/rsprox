package net.rsprox.mcp.server

import com.fasterxml.jackson.databind.JsonNode
import net.rsprox.mcp.bridge.TestHub
import net.rsprox.mcp.packets.Origin
import net.rsprox.mcp.packets.TapSettingSetStore
import net.rsprox.mcp.session.FakeLauncher
import net.rsprox.mcp.session.SessionManager
import net.rsprox.proxy.binary.BinaryHeader
import net.rsprox.shared.StreamDirection
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.TimeUnit
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ToolCallsTest {
    private val launcher = FakeLauncher()
    private val fixture = TestHub()
    private val manager = SessionManager(launcher, TapSettingSetStore, fixture.hub)
    private val server = McpHttpServer(0, tools { manager }, "1.2.3").also { it.start() }
    private val http = HttpClient.newHttpClient()
    private val talkTo = """{"target":"npc","index":0,"option":"Talk-to"}"""

    // Every request the plugin received, as its op and its arguments.
    private val received = CopyOnWriteArrayList<String>()

    // What the plugin answers a request with. It runs before the answer is sent, as the client's packets do.
    @Volatile
    private var answer: (args: JsonNode) -> String = { """"ok":{}""" }

    private val header =
        BinaryHeader(
            headerVersion = 1,
            revision = 235,
            subRevision = 1,
            clientType = 1,
            platformType = 1,
            timestamp = 0,
            worldId = 301,
            worldFlags = 0,
            worldLocation = 0,
            worldHost = "127.0.0.1",
            worldActivity = "",
            localPlayerIndex = 1,
            accountHash = ByteArray(0),
            clientName = "RuneLite",
            js5MasterIndex = ByteArray(0),
        )

    @AfterTest
    fun cleanUp() {
        server.close()
        fixture.close()
    }

    private fun rpc(
        method: String,
        params: String,
    ): JsonNode {
        val body = """{"jsonrpc":"2.0","id":1,"method":"$method","params":$params}"""
        val request =
            HttpRequest
                .newBuilder(URI("http://127.0.0.1:${server.localPort}/mcp"))
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build()

        val response = http.send(request, HttpResponse.BodyHandlers.ofString())

        return McpDispatcher.MAPPER.readTree(response.body()).get("result")
    }

    private fun toolResult(
        tool: String,
        arguments: String,
    ): JsonNode = rpc("tools/call", """{"name":"$tool","arguments":$arguments}""")

    private fun call(
        tool: String,
        arguments: String = "{}",
    ): String {
        val result = toolResult(tool, arguments)
        val text = result.get("content").last().get("text").asText()
        assertFalse(result.get("isError").asBoolean(), text)

        return text
    }

    private fun callJson(
        tool: String,
        arguments: String = "{}",
    ): JsonNode = McpDispatcher.MAPPER.readTree(call(tool, arguments))

    private fun error(
        tool: String,
        arguments: String,
    ): String {
        val result = toolResult(tool, arguments)
        val text = result.get("content").single().get("text").asText()
        assertTrue(result.get("isError").asBoolean(), text)

        return text
    }

    private fun connect() {
        call("session_start", """{"target":"My Server","wait_ms":0}""")
        val plugin = fixture.dial(FakeLauncher.FIRST_HTTP_PORT)
        plugin.read()
        plugin.serve { op, args ->
            received += "$op $args"
            answer(args)
        }

        val started = callJson("session_start", """{"session":"s1","wait_ms":10000}""")
        assertEquals("connected", started.get("state").asText())
    }

    private fun logIn(decoded: Boolean = true) {
        val tap = launcher.monitor.forSession(header)
        tap.onLogin(header)

        if (decoded) tap.onPacketDirection(StreamDirection.CLIENT_TO_SERVER)
    }

    private fun sent(
        prot: String,
        text: String,
        origin: Origin = Origin.CLIENT,
        tick: Int = 7,
    ): Long = manager.resolve("s1").packets.append(1, tick, origin, prot, text)

    private fun clicked(
        x: Int,
        y: Int,
    ): Long =
        sent(
            "EVENT_MOUSE_CLICK_V2",
            "[event_mouse_click_v2] lasttransmitted=40ms, x=$x, y=$y, rightclick=false, code=0",
        )

    private fun performed(
        target: String,
        expect: String,
    ): String =
        """"ok":{"target":"$target","option":"Talk-to","attempts":1,"tick":41,""" +
            """"expect":"$expect","pressed":[312,171],"tried":[]}"""

    private fun rows(arguments: String): List<String> = call("packets_read", arguments).lines().drop(1)

    @Test
    fun `the tool list names the fourteen tools, each with a description and an object schema`() {
        val listed = rpc("tools/list", "{}").get("tools")

        assertEquals(
            listOf(
                "session_start",
                "session_stop",
                "session_list",
                "packets_read",
                "client_state",
                "client_screenshot",
                "client_widgets",
                "client_vars",
                "client_login",
                "client_click",
                "client_type",
                "client_entities",
                "client_interact",
                "client_camera",
            ),
            listed.map { it.get("name").asText() },
        )

        for (tool in listed) {
            assertTrue(tool.get("description").asText().isNotBlank(), tool.toString())
            assertEquals("object", tool.get("inputSchema").get("type").asText(), tool.toString())
        }
    }

    @Test
    fun `a client tool calls the plugin op of its name and returns its answer with the cursor before the call`() {
        connect()
        answer = {
            sent("EVENT_MOUSE_CLICK_V2", "[event_mouse_click_v2] x=10, y=20")
            """"ok":{"x":10,"y":20}"""
        }

        val result = call("client_click", """{"session":"s1","x":10,"y":20,"widget":null}""")

        assertEquals("""{"x":10,"y":20,"cursor":2}""", result)
        assertEquals(listOf("""click {"x":10,"y":20}"""), received)
    }

    @Test
    fun `invalid arguments are refused by name before anything reaches the plugin`() {
        connect()

        assertContains(error("client_click", """{"x":"ten","y":20}"""), "'x' must be of type integer")
        assertContains(error("client_click", """{"x":10,"y":20,"z":1}"""), "unknown argument 'z'")
        assertContains(
            error("client_entities", """{"kinds":["npc","dragon"]}"""),
            "every item of 'kinds' must be one of npc, object",
        )

        assertEquals(emptyList(), received)
    }

    @Test
    fun `an interaction returns the action packet that follows the mouse click of the pressed point`() {
        connect()
        logIn()
        answer = {
            sent("OPNPC1_V2", "[opnpc1_v2] npc=(index=9, id=3010)")
            clicked(312, 171)
            sent("OPNPC1_V2", "[opnpc1_v2] npc=(index=0, id=3308)")
            performed("npc", "OPNPC")
        }

        val result = callJson("client_interact", talkTo)

        assertEquals("6 L1 T7 C OPNPC1_V2 [opnpc1_v2] npc=(index=0, id=3308)", result.get("packet").asText())
        assertEquals("click", result.get("proof").asText())
        assertEquals(3, result.get("cursor").asInt())
        assertFalse(result.has("expect"), result.toString())
        assertFalse(result.has("pressed"), result.toString())
        assertEquals(listOf("interact $talkTo"), received)
    }

    @Test
    fun `an interaction without a mouse click of the pressed point returns the first packet of the expected prefix`() {
        connect()
        logIn()
        answer = {
            clicked(40, 50)
            sent("OPNPC1_V2", "[opnpc1_v2] npc=(index=0, id=3308)")
            sent("OPNPC3_V2", "[opnpc3_v2] npc=(index=0, id=3308)")
            performed("npc", "OPNPC")
        }

        val result = callJson("client_interact", talkTo)

        assertEquals("5 L1 T7 C OPNPC1_V2 [opnpc1_v2] npc=(index=0, id=3308)", result.get("packet").asText())
        assertEquals("prefix", result.get("proof").asText())
    }

    @Test
    fun `an interaction the client sent no packet for fails as dropped in the world and is noted for a widget`() {
        connect()
        logIn()
        answer = { args ->
            if (args.get("target").asText() == "npc") performed("npc", "OPNPC") else performed("widget", "IF_BUTTON")
        }

        // Each call waits three seconds for a packet, so the two wait side by side.
        val npc = CompletableFuture.supplyAsync { error("client_interact", talkTo) }
        val widget = callJson("client_interact", """{"target":"widget","widget":"558:7","option":"Toggle"}""")

        assertContains(npc.get(10, TimeUnit.SECONDS), "no OPNPC packet")
        assertContains(npc.get(), "the action was dropped. Likely cause: the NPC left the client's view")
        assertTrue(widget.get("packet").isNull, widget.toString())
        assertContains(widget.get("note").asText(), "sent no packet for it")
        assertFalse(widget.has("proof"), widget.toString())
    }

    @Test
    fun `a widget interaction is proven by the packet of a dialog option and by that of a close button`() {
        connect()
        logIn()
        answer = {
            clicked(312, 171)
            sent("RESUME_PAUSEBUTTON", "[resume_pausebutton] com=219:1")
            performed("widget", "IF_BUTTON")
        }

        val option = callJson("client_interact", """{"target":"dialog","option":"2"}""")

        assertEquals("5 L1 T7 C RESUME_PAUSEBUTTON [resume_pausebutton] com=219:1", option.get("packet").asText())
        assertEquals("click", option.get("proof").asText())

        answer = {
            clicked(312, 171)
            sent("CLOSE_MODAL", "[close_modal]")
            performed("widget", "IF_BUTTON")
        }

        val close = callJson("client_interact", """{"target":"widget","widget":"12:2","option":"Close"}""")

        assertEquals("7 L1 T7 C CLOSE_MODAL [close_modal]", close.get("packet").asText())
        assertEquals("click", close.get("proof").asText())
    }

    @Test
    fun `an interaction on a login whose packets are not decoded returns unconfirmed without waiting`() {
        connect()
        logIn(decoded = false)
        answer = { performed("npc", "OPNPC") }

        val result = callJson("client_interact", talkTo)

        assertTrue(result.get("packet").isNull, result.toString())
        assertContains(result.get("note").asText(), "not decoded, so the action could not be confirmed")
        assertFalse(result.has("expect"), result.toString())
    }

    @Test
    fun `packets are read as a meta line and one row each, filtered by cursor, prot, origin and text`() {
        connect()
        logIn()
        sent("OPNPC1_V2", "[opnpc1_v2] npc=(index=0, id=3308)")
        sent("IF_SETTEXT", "[if_settext] com=558:7, text=Hello there", Origin.SERVER, tick = 8)
        sent("IF_BUTTONX", "[if_buttonx] com=558:7, op=1", tick = 8)

        assertEquals(
            """
            {"next":6,"head":6,"dropped":0,"timedOut":false,"count":4}
            3 L1 T0 P LOGIN revision=235 world=301
            4 L1 T7 C OPNPC1_V2 [opnpc1_v2] npc=(index=0, id=3308)
            5 L1 T8 S IF_SETTEXT [if_settext] com=558:7, text=Hello there
            6 L1 T8 C IF_BUTTONX [if_buttonx] com=558:7, op=1
            """.trimIndent(),
            call("packets_read", """{"after":2}"""),
        )

        assertEquals(listOf("5", "6"), rows("""{"after":4}""").map { it.substringBefore(' ') })
        assertEquals(listOf("5"), rows("""{"prots":["if_settext"]}""").map { it.substringBefore(' ') })
        assertEquals(listOf("4", "6"), rows("""{"origin":"client"}""").map { it.substringBefore(' ') })
        assertEquals(listOf("5"), rows("""{"contains":"hello THERE"}""").map { it.substringBefore(' ') })
    }

    @Test
    fun `a packet read that waits returns once a matching packet is appended and reports a timeout when none is`() {
        connect()
        logIn()
        val read = """{"after":3,"contains":"farewell","wait_ms":60000}"""
        val waiting = CompletableFuture.supplyAsync { call("packets_read", read) }

        sent("IF_SETTEXT", "[if_settext] com=558:7, text=Hello there", Origin.SERVER)
        sent("IF_SETTEXT", "[if_settext] com=558:7, text=Farewell", Origin.SERVER)

        assertEquals(
            """
            {"next":5,"head":5,"dropped":0,"timedOut":false,"count":1}
            5 L1 T7 S IF_SETTEXT [if_settext] com=558:7, text=Farewell
            """.trimIndent(),
            waiting.get(10, TimeUnit.SECONDS),
        )

        assertEquals(
            """{"next":5,"head":5,"dropped":0,"timedOut":true,"count":0}""",
            call("packets_read", """{"after":5,"wait_ms":50}"""),
        )
    }
}
