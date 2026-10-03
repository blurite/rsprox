package net.rsprox.mcp.server

import com.fasterxml.jackson.databind.JsonNode
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

    @AfterTest
    fun cleanUp() {
        fixture.close()
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
    fun `the four tools are listed`() {
        val body = dispatcher.handle("POST", null, """{"jsonrpc":"2.0","id":1,"method":"tools/list"}""").body
        val names = mapper.readTree(body).get("result").get("tools").map { it.get("name").asText() }
        assertEquals(listOf("session_start", "session_stop", "session_list", "packets_read"), names)
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
}
