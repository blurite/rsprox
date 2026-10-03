package net.rsprox.mcp.server

import com.fasterxml.jackson.databind.JsonNode
import net.rsprox.mcp.bridge.FakePlugin
import net.rsprox.mcp.bridge.TestHub
import net.rsprox.mcp.packets.Origin
import net.rsprox.mcp.packets.TapSettingSetStore
import net.rsprox.mcp.session.FakeLauncher
import net.rsprox.mcp.session.SessionManager
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ToolsTest {
    private val mapper = McpDispatcher.MAPPER
    private val fixture = TestHub()
    private val manager = SessionManager(FakeLauncher(), TapSettingSetStore, fixture.hub)
    private val dispatcher = McpDispatcher(tools { manager }, "test")

    private val plugins = ArrayList<FakePlugin>()

    @AfterTest
    fun cleanUp() {
        plugins.forEach { it.close() }
        fixture.close()
    }

    /** Session s1 with a connected client that answers every op through [answer]. */
    private fun connect(answer: (op: String, args: JsonNode) -> String) {
        manager.start(null, null, 0)
        val plugin = FakePlugin.dial(fixture.rendezvous, 43650).also { plugins += it }
        plugin.read()
        assertEquals("connected", manager.start(null, "s1", 10_000).state)
        plugin.serve(answer)
    }

    private fun call(
        name: String,
        arguments: String = "{}",
    ): JsonNode {
        val body = """{"jsonrpc":"2.0","id":1,"method":"tools/call","params":{"name":"$name","arguments":$arguments}}"""
        return mapper.readTree(dispatcher.handle("POST", null, body).body).get("result")
    }

    private fun text(
        name: String,
        arguments: String = "{}",
    ): String {
        val result = call(name, arguments)
        assertFalse(result.get("isError").asBoolean(), result.toString())
        return result.get("content")[0].get("text").asText()
    }

    @Test
    fun `the tools are listed`() {
        val body = dispatcher.handle("POST", null, """{"jsonrpc":"2.0","id":1,"method":"tools/list"}""").body
        val names = mapper.readTree(body).get("result").get("tools").map { it.get("name").asText() }
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
            ),
            names,
        )
    }

    @Test
    fun `session start, list and stop report the session as compact json`() {
        assertEquals(
            """{"session":"s1","target":"My Server","state":"launching","generation":1,"proxyPort":43751,""" +
                """"httpPort":43650,"cursor":1}""",
            text("session_start", """{"target":"My Server","wait_ms":0}"""),
        )
        val list = mapper.readTree(text("session_list"))
        assertEquals("s1", list.get("sessions").single().get("session").asText())
        assertEquals(listOf("Old School RuneScape", "My Server"), list.get("targets").map { it.asText() })

        val stopped = mapper.readTree(text("session_stop"))
        assertEquals("stopped", stopped.get("state").asText())
        assertEquals("stopped by caller", stopped.get("reason").asText())
    }

    @Test
    fun `packets read prints a meta line and one line per packet`() {
        text("session_start", """{"wait_ms":0}""")
        val log = manager.resolve("s1").packets
        log.append(1, 48, Origin.CLIENT, "RESUME_P_STRINGDIALOG", "[resume_p_stringdialog] string=\"McpProto\"")
        log.append(1, 49, Origin.SERVER, "PLAYER_INFO", "[player_info]\n    [localplayer] index=1\n        - move")

        assertEquals(
            """
            {"next":3,"head":3,"dropped":0,"timedOut":false,"count":3}
            1 L0 T0 P CLIENT_LAUNCHED generation=1 proxyPort=43751 httpPort=43650
            2 L1 T48 C RESUME_P_STRINGDIALOG [resume_p_stringdialog] string="McpProto"
            3 L1 T49 S PLAYER_INFO [player_info]
                    [localplayer] index=1
                        - move
            """.trimIndent(),
            text("packets_read"),
        )
    }

    @Test
    fun `packets read applies its filters and cursor`() {
        text("session_start", """{"wait_ms":0}""")
        val log = manager.resolve("s1").packets
        log.append(1, 1, Origin.CLIENT, "IF_BUTTON", "[if_button] com=558:7")
        log.append(1, 2, Origin.SERVER, "IF_SETTEXT", "[if_settext] text=\"McpProto is available\"")

        val byProt = text("packets_read", """{"prots":["if_settext"]}""").lines()
        assertEquals("""{"next":3,"head":3,"dropped":0,"timedOut":false,"count":1}""", byProt[0])
        assertTrue(byProt[1].startsWith("3 L1 T2 S IF_SETTEXT "))

        assertEquals(2, text("packets_read", """{"origin":"client"}""").lines().size)
        assertEquals(2, text("packets_read", """{"origin":"proxy","contains":"generation=1"}""").lines().size)
        assertEquals(2, text("packets_read", """{"after":2}""").lines().size)
        assertEquals(3, text("packets_read", """{"limit":2}""").lines().size)

        val waited = text("packets_read", """{"after":3,"wait_ms":20}""")
        assertEquals("""{"next":3,"head":3,"dropped":0,"timedOut":true,"count":0}""", waited)
    }

    @Test
    fun `a tool call before any session exists tells the caller what to do`() {
        val result = call("packets_read")
        assertTrue(result.get("isError").asBoolean())
        assertEquals("no session exists yet; call session_start first", result.get("content")[0].get("text").asText())
    }

    @Test
    fun `an origin outside the allowed set is refused`() {
        text("session_start", """{"wait_ms":0}""")
        assertTrue(call("packets_read", """{"origin":"sideways"}""").get("isError").asBoolean())
    }

    @Test
    fun `a client tool returns the plugin's answer with the cursor taken before the call`() {
        val forwarded = ArrayList<Pair<String, JsonNode>>()
        connect { op, args ->
            forwarded += op to args
            // What the action causes arrives while the call is in flight.
            manager.resolve("s1").packets.append(1, 5, Origin.SERVER, "IF_SETTEXT", "[if_settext] text=\"hello\"")
            """"ok":{"gameState":"LOGIN_SCREEN","tick":0}"""
        }
        val log = manager.resolve("s1").packets
        val before = log.head().seq

        val result = text("client_state", """{"session":"s1"}""")

        assertEquals("""{"gameState":"LOGIN_SCREEN","tick":0,"cursor":$before}""", result)
        assertEquals(before + 1, log.head().seq)
        assertEquals(listOf("state" to mapper.readTree("{}")), forwarded)
    }

    @Test
    fun `a client tool forwards its arguments without the session`() {
        val forwarded = ArrayList<Pair<String, JsonNode>>()
        connect { op, args ->
            forwarded += op to args
            """"ok":{"roots":[548],"widgets":[],"truncated":false}"""
        }

        text("client_widgets", """{"session":"s1","group":558,"text":"name","hidden":true,"limit":5}""")
        text("client_vars", """{"varps":[1055],"varbits":[8119]}""")

        assertEquals(
            listOf(
                "widgets" to mapper.readTree("""{"group":558,"text":"name","hidden":true,"limit":5}"""),
                "vars" to mapper.readTree("""{"varps":[1055],"varbits":[8119]}"""),
            ),
            forwarded,
        )
    }

    @Test
    fun `a screenshot is an image block followed by its size and the cursor`() {
        connect { _, _ -> """"ok":{"png":"iVBORw0KGgo=","width":976,"height":558}""" }
        val cursor = manager.resolve("s1").packets.head().seq

        val content = call("client_screenshot").get("content")

        assertEquals(
            mapper.readTree(
                """[{"type":"image","data":"iVBORw0KGgo=","mimeType":"image/png"},
                {"type":"text","text":"{\"width\":976,\"height\":558,\"cursor\":$cursor}"}]""",
            ),
            content,
        )
    }

    @Test
    fun `a failure reported by the client becomes a tool error with its code`() {
        connect { _, _ -> """"err":{"code":"not_found","message":"varbit 99999 could not be read"}""" }

        val result = call("client_vars", """{"varbits":[99999]}""")

        assertTrue(result.get("isError").asBoolean())
        assertEquals("not_found: varbit 99999 could not be read", result.get("content")[0].get("text").asText())
    }

    @Test
    fun `a client tool on a session without a connected client says how to get one`() {
        text("session_start", """{"wait_ms":0}""")
        val launching = call("client_state")
        assertTrue(launching.get("isError").asBoolean())
        assertEquals(
            "session s1 is still launching; call session_start with this session to wait for it",
            launching.get("content")[0].get("text").asText(),
        )

        text("session_stop")
        assertEquals(
            "session s1 has no connected client: stopped by caller",
            call("client_state").get("content")[0].get("text").asText(),
        )
    }

    @Test
    fun `a client tool refuses arguments of the wrong type before calling the client`() {
        val calls = ArrayList<String>()
        connect { op, _ ->
            calls += op
            """"ok":{}"""
        }

        assertTrue(call("client_vars", """{"varps":["1055"]}""").get("isError").asBoolean())
        assertTrue(call("client_widgets", """{"hidden":"yes"}""").get("isError").asBoolean())
        assertEquals(emptyList(), calls)
    }

    @Test
    fun `the input tools forward their arguments and return the client's answer`() {
        val forwarded = ArrayList<Pair<String, JsonNode>>()
        connect { op, args ->
            forwarded += op to args
            when (op) {
                "login" -> """"ok":{"gameState":"LOGGED_IN"}"""
                "click" -> """"ok":{"x":380,"y":215}"""
                else -> """"ok":{"typed":8}"""
            }
        }
        val cursor = manager.resolve("s1").packets.head().seq

        assertEquals(
            """{"gameState":"LOGGED_IN","cursor":$cursor}""",
            text("client_login", """{"username":"mcp","wait_ms":2000}"""),
        )
        assertEquals("""{"x":380,"y":215,"cursor":$cursor}""", text("client_click", """{"widget":"558:7"}"""))
        text("client_click", """{"session":"s1","x":380,"y":215,"button":"right"}""")
        assertEquals("""{"typed":8,"cursor":$cursor}""", text("client_type", """{"text":"McpProto","enter":true}"""))

        assertEquals(
            listOf(
                "login" to mapper.readTree("""{"username":"mcp","wait_ms":2000}"""),
                "click" to mapper.readTree("""{"widget":"558:7"}"""),
                "click" to mapper.readTree("""{"x":380,"y":215,"button":"right"}"""),
                "type" to mapper.readTree("""{"text":"McpProto","enter":true}"""),
            ),
            forwarded,
        )
    }

    @Test
    fun `a login needs a username and accepts a missing password`() {
        val calls = ArrayList<JsonNode>()
        connect { _, args ->
            calls.add(args)
            """"ok":{"gameState":"LOGGED_IN"}"""
        }

        val missing = call("client_login", """{"password":"secret"}""")
        assertTrue(missing.get("isError").asBoolean())
        assertEquals(
            "client_login: missing required argument 'username'",
            missing.get("content")[0].get("text").asText(),
        )

        text("client_login", """{"username":"mcp"}""")
        assertEquals(listOf(mapper.readTree("""{"username":"mcp"}""")), calls)
    }

    @Test
    fun `a button other than left or right is refused`() {
        connect { _, _ -> """"ok":{}""" }
        assertTrue(call("client_click", """{"x":1,"y":1,"button":"middle"}""").get("isError").asBoolean())
    }
}
