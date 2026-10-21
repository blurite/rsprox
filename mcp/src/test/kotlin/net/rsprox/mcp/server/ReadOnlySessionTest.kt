package net.rsprox.mcp.server

import net.rsprox.mcp.bridge.Access
import net.rsprox.mcp.session.FakeLauncher
import net.rsprox.mcp.session.target
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class ReadOnlySessionTest {
    private val server = ToolServer()

    // Every request a plugin received, as its session, its op and its arguments.
    private val received = CopyOnWriteArrayList<String>()

    /** One client tool with arguments that pass its schema, and the op the plugin runs for it. */
    private class Call(
        val tool: String,
        val access: Access,
        val op: String,
        val arguments: String = "{}",
    )

    private val calls =
        listOf(
            Call("client_state", Access.READ, "state"),
            Call("client_screenshot", Access.READ, "screenshot"),
            Call("client_widgets", Access.READ, "widgets"),
            Call("client_vars", Access.READ, "vars"),
            Call("client_entities", Access.READ, "entities"),
            Call("client_camera", Access.READ, "camera"),
            Call("client_login", Access.DRIVE, "login", """{"username":"alice","password":"any"}"""),
            Call("client_click", Access.DRIVE, "click", """{"x":10,"y":20}"""),
            Call("client_type", Access.DRIVE, "type", """{"text":"hello"}"""),
            Call("client_interact", Access.DRIVE, "interact", """{"target":"tile","x":3094,"y":3107}"""),
            Call("client_camera", Access.DRIVE, "camera_turn", """{"yaw":100}"""),
            Call("client_camera", Access.DRIVE, "camera_turn", """{"pitch":2048}"""),
            Call("client_camera", Access.DRIVE, "camera_turn", """{"look_at":"npc","index":0}"""),
        )

    @AfterTest
    fun cleanUp() {
        server.close()
    }

    /** Launches a session of the target and connects a plugin that answers every op with an empty object. */
    private fun connect(target: String): String {
        val session = server.callJson("session_start", """{"target":"$target","wait_ms":0}""").get("session").asText()
        val plugin = server.hub.dial(FakeLauncher.FIRST_HTTP_PORT + server.launcher.reserved.size - 1)
        plugin.read()
        plugin.serve { op, args ->
            received += "$session $op $args"
            if (op == "interact") """"ok":{"expect":"MOVE_GAMECLICK","pressed":[1,2]}""" else """"ok":{"png":""}"""
        }

        server.call("session_start", """{"session":"$session","wait_ms":10000}""")

        return session
    }

    /** Calls a tool that must succeed, whatever content it returns. */
    private fun succeed(call: Call) {
        val result = server.rpc("tools/call", """{"name":"${call.tool}","arguments":${call.arguments}}""")

        assertFalse(result.get("isError").asBoolean(), result.toString())
    }

    private fun refusal(
        tool: String,
        session: String,
    ): String =
        "$tool sends input to the client, which is refused for session $session: its target " +
            "'Old School RuneScape' is the official game, so the session is read-only. Use the tools that read: " +
            "client_state, client_screenshot, client_widgets, client_vars, client_entities, client_camera without " +
            "yaw, pitch or look_at, and packets_read."

    @Test
    fun `every client tool reaches a client of a custom target`() {
        val session = connect("My Server")

        calls.forEach(::succeed)

        assertEquals(calls.map { "$session ${call(it)}" }, received)
    }

    @Test
    fun `on the official game the tools that read reach the client and the tools that send input are refused`() {
        val session = connect("Old School RuneScape")

        for (call in calls.filter { it.access == Access.DRIVE }) {
            assertEquals(refusal(call.tool, session), server.error(call.tool, call.arguments))
        }

        assertEquals(emptyList(), received)

        calls.filter { it.access == Access.READ }.forEach(::succeed)

        assertEquals(calls.filter { it.access == Access.READ }.map { "$session ${call(it)}" }, received)

        for (read in received.map { it.substringAfter(' ').substringBefore(' ') }) {
            assertContains(refusal("client_click", session), "client_$read")
        }
    }

    @Test
    fun `an official session refuses input before it says that it has no client`() {
        server.call("session_start", """{"target":"Old School RuneScape","wait_ms":0}""")

        assertEquals(refusal("client_type", "s1"), server.error("client_type", """{"text":"hello"}"""))
        assertEquals(
            "session s1 is still launching; call session_start with this session to wait for it",
            server.error("client_state", "{}"),
        )
    }

    @Test
    fun `the session list says whether each session can be driven`() {
        server.call("session_start", """{"target":"My Server","wait_ms":0}""")
        server.call("session_start", """{"target":"Old School RuneScape","wait_ms":0}""")
        server.manager.attach(43701, target(1, "My Server"))

        assertEquals(
            listOf("s1 launched drive", "s2 launched read", "s3 attached read"),
            server.callJson("session_list").get("sessions").map {
                "${it.get("session").asText()} ${it.get("kind").asText()} ${it.get("access").asText()}"
            },
        )
    }

    @Test
    fun `a target that takes its jav_config from the official game is read-only whatever its id`() {
        val mirror = target(1, "Mirror").copy(javConfigUrl = "https://oldschool.config.runescape.com/jav_config.ws")
        server.launcher.targets = listOf(target(0, "Old School RuneScape"), mirror, target(2, "My Server"))

        val started = server.callJson("session_start", """{"wait_ms":0}""")
        val forced = server.callJson("session_start", """{"target":"Mirror","wait_ms":0}""")

        assertEquals("My Server drive", "${started.get("target").asText()} ${started.get("access").asText()}")
        assertEquals("Mirror read", "${forced.get("target").asText()} ${forced.get("access").asText()}")
    }

    /** The op and the arguments the plugin receives for the call. */
    private fun call(call: Call): String {
        val forwarded =
            when (call.tool) {
                "client_widgets" -> """{"limit":200}"""
                "client_entities" -> """{"radius":15,"limit":100}"""
                "client_login" -> """{"username":"alice","password":"any","wait_ms":15000}"""
                else -> call.arguments
            }

        return "${call.op} $forwarded"
    }
}
