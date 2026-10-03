package net.rsprox.mcpbridge

import com.fasterxml.jackson.databind.JsonNode
import com.google.gson.Gson
import net.rsprox.mcp.bridge.BridgeError
import net.rsprox.mcp.bridge.Rendering as ServerRendering
import net.rsprox.mcp.bridge.TestHub
import net.rsprox.mcp.bridge.awaitTrue
import net.rsprox.mcp.packets.PacketQuery
import net.rsprox.mcp.packets.TapSettingSetStore
import net.rsprox.mcp.server.McpDispatcher
import net.rsprox.mcp.server.minimalArguments
import net.rsprox.mcp.server.tools
import net.rsprox.mcp.session.FakeLauncher
import net.rsprox.mcp.session.SessionManager
import net.runelite.api.GameState
import net.runelite.api.MenuAction
import java.awt.Color
import java.awt.Rectangle
import java.awt.event.KeyEvent
import java.awt.image.BufferedImage
import java.nio.file.Path
import java.util.Base64
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import javax.imageio.ImageIO
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * The server and the plugin together: every tool call crosses the real hub, a loopback socket and the
 * real plugin, and ends in a stand-in for the game.
 */
class BridgeEndToEndTest {
    private val mapper = McpDispatcher.MAPPER
    private val fixture = TestHub()
    private val launcher = FakeLauncher()
    private val manager = SessionManager(launcher, TapSettingSetStore, fixture.hub)
    private val dispatcher = McpDispatcher(tools { manager }, "test")
    private val game = FakeGame()
    private val connections = ArrayList<BridgeConnection>()

    @AfterTest
    fun cleanUp() {
        connections.forEach { it.close() }
        fixture.close()
        game.close()
    }

    /** Starts the plugin as a launched client does. Null when the plugin stayed dormant. */
    private fun startPlugin(
        rendezvous: Path,
        httpPort: Int,
    ): BridgeConnection? {
        val ops = Ops(GameAccess(game.client, game.clientThread, game.frames))

        return BridgeConnection.dial(rendezvous, httpPort, Gson(), ops)?.also { connections += it }
    }

    /** Session s1, launched and connected to the plugin. */
    private fun connect(): BridgeConnection {
        launcher.launch = { startPlugin(fixture.rendezvous, 43650) }
        assertEquals("connected", manager.start(null, null, 10_000).state)

        return connections.single()
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

    private fun failure(
        name: String,
        arguments: String = "{}",
    ): String {
        val result = call(name, arguments)
        assertTrue(result.get("isError").asBoolean(), result.toString())

        return result.get("content")[0].get("text").asText()
    }

    private fun cursor(): Long = manager.resolve("s1").packets.head().seq

    @Test
    fun `a launched client says hello and its session connects with the client's pid`() {
        launcher.launch = { startPlugin(fixture.rendezvous, 43650) }

        val started = mapper.readTree(text("session_start", """{"wait_ms":10000}"""))

        assertEquals("connected", started.get("state").asText())
        assertEquals(ProcessHandle.current().pid(), started.get("pid").asLong())
        assertEquals(Rendering.GPU, connections.single().rendering())
        assertEquals(
            listOf("CLIENT_LAUNCHED", "CLIENT_CONNECTED"),
            manager.resolve("s1").packets.read(PacketQuery()).packets.map { it.prot },
        )
    }

    @Test
    fun `a server set to software rendering tells the plugin it welcomes`() {
        TestHub(ServerRendering.SOFTWARE).use { software ->
            val softwareManager = SessionManager(FakeLauncher(), TapSettingSetStore, software.hub)
            softwareManager.start(null, null, 0)

            val connection = startPlugin(software.rendezvous, 43650)

            assertEquals(Rendering.SOFTWARE, connection?.rendering())
            assertEquals("connected", softwareManager.start(null, "s1", 10_000).state)
        }
    }

    @Test
    fun `a client that no session expects stays dormant`() {
        manager.start(null, null, 0)

        assertNull(startPlugin(fixture.rendezvous, 5))

        assertEquals("launching", manager.list().single().state)
    }

    @Test
    fun `a client that rsprox did not launch stays dormant`() {
        manager.start(null, null, 0)

        assertNull(startPlugin(fixture.rendezvous, -1))

        assertEquals("launching", manager.list().single().state)
    }

    @Test
    fun `a client stays dormant while no server has left a rendezvous file`() {
        assertNull(startPlugin(fixture.rendezvous.resolveSibling("absent.json"), 43650))
    }

    @Test
    fun `a plugin that shuts down stops its session`() {
        val connection = connect()

        connection.close()

        awaitTrue("the session is stopped") { manager.list().single().state == "stopped" }
        assertEquals("client exited", manager.list().single().reason)
    }

    @Test
    fun `state on the login screen has no player`() {
        connect()

        assertEquals(
            """{"gameState":"LOGIN_SCREEN","tick":0,"canvas":[765,503],"world":301,"player":null,""" +
                """"menu":{"open":false,"entries":[{"option":"Cancel","target":"","type":"CANCEL","id":0,""" +
                """"p0":0,"p1":0}]},"cursor":${cursor()}}""",
            text("client_state"),
        )
    }

    @Test
    fun `state in the game describes the player and the open menu`() {
        connect()
        game.logIn("McpProto")
        game.menuOpen = true
        game.menu =
            listOf(
                menuEntry("Cancel", "", MenuAction.CANCEL, 0, 0, 0),
                menuEntry("Talk-to", "<col=ffff00>Guide", MenuAction.NPC_FIRST_OPTION, 3308, 52, 49),
            )

        val state = mapper.readTree(text("client_state"))

        assertEquals("LOGGED_IN", state.get("gameState").asText())
        assertEquals(mapper.readTree("""{"name":"McpProto","x":3222,"y":3218,"plane":0}"""), state.get("player"))
        assertTrue(state.get("menu").get("open").asBoolean())
        assertEquals(
            mapper.readTree(
                """{"option":"Talk-to","target":"<col=ffff00>Guide","type":"NPC_FIRST_OPTION","id":3308,
                "p0":52,"p1":49}""",
            ),
            state.get("menu").get("entries")[1],
        )
    }

    /** Interface 558 under root 548: a button, a name field that is a dynamic child, and a hidden label. */
    private fun showDisplayNameInterface() {
        val button = widget(558, 7, text = "Look up name", actions = listOf("Select", null, ""))
        val field = widget(558, 12, index = 3, name = "Name field", bounds = Rectangle(467, 350, 332, 22))
        val layer = widget(558, 12, dynamic = listOf(field))
        val status = widget(558, 13, text = "Enter a name", hidden = true)
        val container = widget(558, 0, static = listOf(button, layer, status))

        game.roots = listOf(widget(548, 0, text = "Root", nested = listOf(container)))
    }

    @Test
    fun `widgets lists what can be read or clicked, with its bounds and click point`() {
        connect()
        showDisplayNameInterface()

        assertEquals(
            """{"roots":[548],"widgets":[""" +
                """{"id":"548:0","text":"Root","name":"","actions":[],"bounds":[0,0,10,10],"click":[5,5],""" +
                """"type":4,"hidden":false},""" +
                """{"id":"558:7","text":"Look up name","name":"","actions":["Select"],"bounds":[0,0,10,10],""" +
                """"click":[5,5],"type":4,"hidden":false},""" +
                """{"id":"558:12[3]","text":"","name":"Name field","actions":[],"bounds":[467,350,332,22],""" +
                """"click":[633,361],"type":4,"hidden":false}""" +
                """],"truncated":false,"cursor":${cursor()}}""",
            text("client_widgets"),
        )
    }

    @Test
    fun `widgets narrows the list by text, by group and to hidden ones`() {
        connect()
        showDisplayNameInterface()

        fun ids(arguments: String): List<String> =
            mapper.readTree(text("client_widgets", arguments)).get("widgets").map { it.get("id").asText() }

        assertEquals(listOf("558:7", "558:12[3]"), ids("""{"text":"NAME"}"""))
        assertEquals(listOf("558:7"), ids("""{"text":"select"}"""))
        assertEquals(listOf("548:0"), ids("""{"group":548}"""))
        assertEquals(listOf("558:7", "558:12[3]", "558:13"), ids("""{"group":558,"hidden":true}"""))
    }

    @Test
    fun `widgets stops at the limit and says the list was cut short`() {
        connect()
        showDisplayNameInterface()

        val listed = mapper.readTree(text("client_widgets", """{"limit":2}"""))

        assertEquals(listOf("548:0", "558:7"), listed.get("widgets").map { it.get("id").asText() })
        assertTrue(listed.get("truncated").asBoolean())
    }

    @Test
    fun `vars reads each requested id`() {
        connect()
        game.varps[1055] = 7
        game.varbits[8119] = 1
        game.varcInts[5] = 0
        game.varcStrs[335] = "McpProto"
        game.varcStrs[336] = null

        assertEquals(
            """{"varps":{"1055":7},"varbits":{"8119":1},"varcInts":{"5":0},""" +
                """"varcStrs":{"335":"McpProto","336":null},"cursor":${cursor()}}""",
            text("client_vars", """{"varps":[1055],"varbits":[8119],"varcInts":[5],"varcStrs":[335,336]}"""),
        )
    }

    @Test
    fun `vars reports an id the client does not have as not found`() {
        connect()

        val message = failure("client_vars", """{"varbits":[99999]}""")

        assertTrue(message.startsWith("not_found: varbit 99999 could not be read"), message)
    }

    @Test
    fun `login submits the credentials and returns once the client is in the game`() {
        connect()

        assertEquals(
            """{"gameState":"LOGGED_IN","cursor":${cursor()}}""",
            text("client_login", """{"username":"mcptest","password":"secret"}"""),
        )
        assertEquals("mcptest" to "secret", game.username to game.password)
    }

    @Test
    fun `login waits for a client that is still starting to reach the login screen`() {
        connect()
        game.startingReads = 2

        text("client_login", """{"username":"mcptest","password":"secret"}""")

        assertEquals("mcptest", game.username)
        assertEquals(GameState.LOGGED_IN, game.gameState)
    }

    @Test
    fun `a login the server refuses is reported as the wrong state`() {
        connect()
        game.loginAnswer = GameState.LOGIN_SCREEN

        assertEquals(
            "wrong_state: the login was refused; the client is back on the login screen",
            failure("client_login", """{"username":"mcptest","password":"wrong"}"""),
        )
    }

    @Test
    fun `a login times out when the client never reaches the login screen`() {
        connect()
        game.gameState = GameState.STARTING

        assertEquals(
            "timeout: the client did not reach the login screen; gameState is STARTING. " +
                "A client that cannot reach the game server stays in this state.",
            failure("client_login", """{"username":"mcptest","password":"secret","wait_ms":0}"""),
        )
        assertNull(game.username)
    }

    @Test
    fun `a login times out when the client stays logging in`() {
        connect()
        game.loginAnswer = GameState.LOGGING_IN

        assertEquals(
            "timeout: not logged in before the wait elapsed; gameState is LOGGING_IN",
            failure("client_login", """{"username":"mcptest","password":"secret","wait_ms":0}"""),
        )
    }

    @Test
    fun `a login from past the login screen is refused without touching the credentials`() {
        connect()
        game.logIn("McpProto")

        assertEquals(
            "wrong_state: the client is not on the login screen: LOGGED_IN",
            failure("client_login", """{"username":"mcptest","password":"secret"}"""),
        )
        assertNull(game.username)
    }

    @Test
    fun `an empty password is a bad argument`() {
        connect()

        assertEquals(
            "bad_args: password is required and must not be empty",
            failure("client_login", """{"username":"mcptest","password":""}"""),
        )
    }

    @Test
    fun `a call that waits on the game does not hold up the next one`() {
        connect()
        game.loginAnswer = GameState.LOGGING_IN
        val login =
            CompletableFuture.supplyAsync {
                text("client_login", """{"username":"mcptest","password":"secret","wait_ms":60000}""")
            }

        awaitTrue("the login is submitted") { game.gameState == GameState.LOGGING_IN }

        assertEquals("LOGGING_IN", mapper.readTree(text("client_state")).get("gameState").asText())
        assertFalse(login.isDone)

        game.gameState = GameState.LOGGED_IN
        assertEquals("LOGGED_IN", mapper.readTree(login.get(10, TimeUnit.SECONDS)).get("gameState").asText())
    }

    @Test
    fun `a click at coordinates moves, presses, releases and clicks the left button there`() {
        connect()

        assertEquals("""{"x":380,"y":215,"cursor":${cursor()}}""", text("client_click", """{"x":380,"y":215}"""))

        assertEquals(
            listOf(
                "moved 380,215 button 0",
                "pressed 380,215 button 1 holding 1",
                "released 380,215 button 1",
                "clicked 380,215 button 1",
            ),
            game.canvas.events,
        )
    }

    @Test
    fun `a right click uses the right button`() {
        connect()

        text("client_click", """{"x":10,"y":20,"button":"right"}""")

        assertEquals(
            listOf(
                "moved 10,20 button 0",
                "pressed 10,20 button 3 holding 3",
                "released 10,20 button 3",
                "clicked 10,20 button 3",
            ),
            game.canvas.events,
        )
    }

    @Test
    fun `a click on a widget lands on its centre`() {
        connect()
        showDisplayNameInterface()

        assertEquals("""{"x":5,"y":5,"cursor":${cursor()}}""", text("client_click", """{"widget":"558:7"}"""))
        assertEquals("""{"x":633,"y":361,"cursor":${cursor()}}""", text("client_click", """{"widget":"558:12[3]"}"""))

        assertEquals(
            listOf("pressed 5,5 button 1 holding 1", "pressed 633,361 button 1 holding 1"),
            game.canvas.events.filter { it.startsWith("pressed") },
        )
    }

    @Test
    fun `a click on a widget that is missing or hidden is not found and sends no input`() {
        connect()
        showDisplayNameInterface()

        assertEquals("not_found: widget 558:99 does not exist", failure("client_click", """{"widget":"558:99"}"""))
        assertEquals("not_found: widget 558:7[9] does not exist", failure("client_click", """{"widget":"558:7[9]"}"""))
        assertEquals("not_found: widget 558:13 is not visible", failure("client_click", """{"widget":"558:13"}"""))
        assertEquals(emptyList(), game.canvas.events)
    }

    @Test
    fun `a click without a usable target is a bad argument`() {
        connect()

        assertEquals("bad_args: pass either x and y, or widget", failure("client_click", """{"x":380}"""))
        assertEquals(
            """bad_args: widget must look like "558:7" or "558:7[3]"""",
            failure("client_click", """{"widget":"name field"}"""),
        )
        assertEquals(emptyList(), game.canvas.events)
    }

    @Test
    fun `type presses, types and releases each character, then Enter when asked`() {
        connect()

        assertEquals("""{"typed":2,"cursor":${cursor()}}""", text("client_type", """{"text":"m1","enter":true}"""))

        assertEquals(
            listOf(
                "pressed code ${KeyEvent.VK_M} char ${'m'.code}",
                "typed code 0 char ${'m'.code}",
                "released code ${KeyEvent.VK_M} char ${'m'.code}",
                "pressed code ${KeyEvent.VK_1} char ${'1'.code}",
                "typed code 0 char ${'1'.code}",
                "released code ${KeyEvent.VK_1} char ${'1'.code}",
                "pressed code ${KeyEvent.VK_ENTER} char 10",
                "typed code 0 char 10",
                "released code ${KeyEvent.VK_ENTER} char 10",
            ),
            game.canvas.events,
        )
    }

    @Test
    fun `type without enter sends only the text`() {
        connect()

        text("client_type", """{"text":"a"}""")

        assertEquals(3, game.canvas.events.size)
    }

    @Test
    fun `a screenshot is the next frame, scaled to the canvas so a pixel is a click coordinate`() {
        connect()
        val dense = BufferedImage(1530, 1006, BufferedImage.TYPE_INT_RGB)
        val graphics = dense.createGraphics()
        graphics.color = Color.RED
        graphics.fillRect(0, 0, 1530, 1006)
        graphics.dispose()
        game.frame = dense
        val cursor = cursor()

        val content = call("client_screenshot").get("content")

        assertEquals("image/png", content[0].get("mimeType").asText())
        val image = ImageIO.read(Base64.getDecoder().decode(content[0].get("data").asText()).inputStream())
        assertEquals(765 to 503, image.width to image.height)
        assertEquals(Color.RED.rgb, image.getRGB(380, 215))
        assertEquals("""{"width":765,"height":503,"cursor":$cursor}""", content[1].get("text").asText())
    }

    @Test
    fun `an op the plugin does not know is a bad argument`() {
        connect()
        val link = manager.resolve("s1").requireLink()

        val error = assertFailsWith<BridgeError> { link.call("dance", mapper.createObjectNode(), 10_000) }

        assertEquals("bad_args" to "unknown op 'dance'", error.code to error.message)
    }

    @Test
    fun `every client tool has an op in the plugin`() {
        connect()
        val clientTools = tools { manager }.filter { it.name.startsWith("client_") }
        assertTrue(clientTools.isNotEmpty())

        for (tool in clientTools) {
            val answer = call(tool.name, minimalArguments(tool).toString()).get("content").last().get("text").asText()

            assertFalse(answer.contains("unknown op"), "${tool.name}: $answer")
        }
    }
}
