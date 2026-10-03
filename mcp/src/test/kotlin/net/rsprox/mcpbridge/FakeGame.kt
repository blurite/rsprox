package net.rsprox.mcpbridge

import net.runelite.api.Client
import net.runelite.api.GameState
import net.runelite.api.Menu
import net.runelite.api.MenuAction
import net.runelite.api.MenuEntry
import net.runelite.api.Player
import net.runelite.api.coords.WorldPoint
import net.runelite.api.widgets.Widget
import net.runelite.client.ui.DrawManager
import java.awt.Canvas
import java.awt.Image
import java.awt.Rectangle
import java.awt.event.InputEvent
import java.awt.event.KeyEvent
import java.awt.event.KeyListener
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import java.awt.event.MouseMotionListener
import java.awt.image.BufferedImage
import java.lang.reflect.Proxy
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.function.Consumer

/**
 * An instance of the interface [T] that answers the methods named in [answers] and fails on any other,
 * so a test notices a call it did not set up.
 */
internal inline fun <reified T : Any> fake(vararg answers: Pair<String, (arguments: List<Any?>) -> Any?>): T {
    val byName = answers.toMap()
    val type = T::class.java

    return type.cast(
        Proxy.newProxyInstance(type.classLoader, arrayOf(type)) { proxy, method, arguments ->
            when (method.name) {
                in byName -> byName.getValue(method.name)(arguments?.toList().orEmpty())
                "toString" -> "fake ${type.simpleName}"
                "hashCode" -> System.identityHashCode(proxy)
                "equals" -> proxy === arguments[0]
                else -> error("the fake ${type.simpleName} has no answer for ${method.name}")
            }
        },
    )
}

internal fun widget(
    group: Int,
    child: Int,
    index: Int = -1,
    text: String? = null,
    name: String? = null,
    actions: List<String?>? = null,
    bounds: Rectangle = Rectangle(0, 0, 10, 10),
    hidden: Boolean = false,
    static: List<Widget> = emptyList(),
    dynamic: List<Widget> = emptyList(),
    nested: List<Widget>? = null,
): Widget =
    fake(
        "getId" to { (group shl 16) or child },
        "getIndex" to { index },
        "getText" to { text },
        "getName" to { name },
        "getActions" to { actions?.toTypedArray() },
        "getBounds" to { bounds },
        "isHidden" to { hidden },
        "getType" to { 4 },
        "getStaticChildren" to { static.toTypedArray() },
        "getDynamicChildren" to { dynamic.toTypedArray() },
        "getNestedChildren" to { nested?.toTypedArray() },
        "getChild" to { arguments -> dynamic.firstOrNull { it.index == arguments[0] } },
    )

internal fun menuEntry(
    option: String,
    target: String,
    type: MenuAction,
    id: Int,
    param0: Int,
    param1: Int,
): MenuEntry =
    fake(
        "getOption" to { option },
        "getTarget" to { target },
        "getType" to { type },
        "getIdentifier" to { id },
        "getParam0" to { param0 },
        "getParam1" to { param1 },
    )

/** A game canvas with no window, which records the input events it is sent. */
internal class RecordingCanvas(
    width: Int,
    height: Int,
) : Canvas(),
    MouseListener,
    MouseMotionListener,
    KeyListener {
    val events = CopyOnWriteArrayList<String>()

    init {
        setSize(width, height)
        addMouseListener(this)
        addMouseMotionListener(this)
        addKeyListener(this)
    }

    // AWT only delivers key events to a component that is on screen, and one without a window never is.
    override fun isShowing(): Boolean = true

    override fun isDisplayable(): Boolean = true

    private fun mouse(
        kind: String,
        event: MouseEvent,
    ) {
        val held =
            when {
                event.modifiersEx and InputEvent.BUTTON1_DOWN_MASK != 0 -> " holding 1"
                event.modifiersEx and InputEvent.BUTTON3_DOWN_MASK != 0 -> " holding 3"
                else -> ""
            }

        events += "$kind ${event.x},${event.y} button ${event.button}$held"
    }

    private fun key(
        kind: String,
        event: KeyEvent,
    ) {
        events += "$kind code ${event.keyCode} char ${event.keyChar.code}"
    }

    override fun mouseMoved(event: MouseEvent) = mouse("moved", event)

    override fun mousePressed(event: MouseEvent) = mouse("pressed", event)

    override fun mouseReleased(event: MouseEvent) = mouse("released", event)

    override fun mouseClicked(event: MouseEvent) = mouse("clicked", event)

    override fun keyPressed(event: KeyEvent) = key("pressed", event)

    override fun keyTyped(event: KeyEvent) = key("typed", event)

    override fun keyReleased(event: KeyEvent) = key("released", event)

    override fun mouseDragged(event: MouseEvent) {
        //
    }

    override fun mouseEntered(event: MouseEvent) {
        //
    }

    override fun mouseExited(event: MouseEvent) {
        //
    }
}

/**
 * The game as the plugin sees it: a [Client], the thread it must be read on, and the renderer that
 * hands out frames. A test sets the fields to describe the game, and reads them to see what the
 * plugin did to it.
 */
internal class FakeGame : AutoCloseable {
    val canvas = RecordingCanvas(765, 503)

    @Volatile var gameState = GameState.LOGIN_SCREEN

    /** How many more reads of the game state answer STARTING before [gameState] shows. */
    @Volatile var startingReads = 0

    /** The state the game server puts the client in when it submits a login. */
    @Volatile var loginAnswer = GameState.LOGGED_IN

    @Volatile var username: String? = null

    @Volatile var password: String? = null

    @Volatile var player: Player? = null

    @Volatile var menuOpen = false

    @Volatile var menu = listOf(menuEntry("Cancel", "", MenuAction.CANCEL, 0, 0, 0))

    @Volatile var roots = emptyList<Widget>()

    @Volatile var frame = BufferedImage(765, 503, BufferedImage.TYPE_INT_RGB)

    val varps = HashMap<Int, Int>()
    val varbits = HashMap<Int, Int>()
    val varcInts = HashMap<Int, Int>()
    val varcStrs = HashMap<Int, String?>()

    private val gameThread = singleThread("fake-client-thread")
    private val renderThread = singleThread("fake-render-thread")

    val clientThread: Executor = gameThread

    /** Hands [frame] to each requested listener on the render thread, as the client does after a draw. */
    val frames: DrawManager =
        object : DrawManager() {
            override fun requestNextFrameListener(listener: Consumer<Image>) {
                super.requestNextFrameListener(listener)
                renderThread.execute { processDrawComplete { frame } }
            }
        }

    val client: Client =
        fake(
            "getGameState" to { readGameState() },
            "setGameState" to { arguments -> submitLogin(arguments[0] as GameState) },
            "setUsername" to { arguments -> username = arguments[0] as String },
            "setPassword" to { arguments -> password = arguments[0] as String },
            "getTickCount" to { 0 },
            "getCanvas" to { canvas },
            "getWorld" to { 301 },
            "getLocalPlayer" to { player },
            "isMenuOpen" to { menuOpen },
            "getMenu" to { fake<Menu>("getMenuEntries" to { menu.toTypedArray() }) },
            "getWidgetRoots" to { roots.toTypedArray() },
            "getWidget" to { arguments -> find(roots, (arguments[0] as Int shl 16) or arguments[1] as Int) },
            // The client indexes its tables with the id and throws for one that does not exist.
            "getVarpValue" to { arguments -> varps.getValue(arguments[0] as Int) },
            "getVarbitValue" to { arguments -> varbits.getValue(arguments[0] as Int) },
            "getVarcIntValue" to { arguments -> varcInts.getValue(arguments[0] as Int) },
            "getVarcStrValue" to { arguments -> varcStrs.getValue(arguments[0] as Int) },
        )

    fun logIn(name: String) {
        player =
            fake(
                "getName" to { name },
                "getWorldLocation" to { WorldPoint(3222, 3218, 0) },
            )

        gameState = GameState.LOGGED_IN
    }

    override fun close() {
        gameThread.shutdownNow()
        renderThread.shutdownNow()
    }

    private fun readGameState(): GameState {
        if (startingReads == 0) return gameState

        startingReads--

        return GameState.STARTING
    }

    private fun submitLogin(requested: GameState) {
        check(requested == GameState.LOGGING_IN) { "the plugin only ever asks the client to log in" }

        gameState = loginAnswer
    }

    private fun find(
        widgets: List<Widget>,
        id: Int,
    ): Widget? {
        for (widget in widgets) {
            if (widget.id == id && widget.index < 0) return widget

            val below = find(widget.staticChildren.toList() + widget.nestedChildren.orEmpty(), id)
            if (below != null) return below
        }

        return null
    }

    private fun singleThread(name: String): ExecutorService =
        Executors.newSingleThreadExecutor { runnable ->
            Thread(runnable, name).apply { isDaemon = true }
        }
}
