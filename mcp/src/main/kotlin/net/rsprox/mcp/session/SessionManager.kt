package net.rsprox.mcp.session

import net.rsprox.mcp.bridge.BridgeHub
import net.rsprox.mcp.bridge.BridgeLink
import net.rsprox.mcp.bridge.BridgeListener
import net.rsprox.mcp.packets.PacketTap
import net.rsprox.mcp.server.ToolError
import net.rsprox.proxy.binary.BinaryHeader
import net.rsprox.proxy.target.ProxyTargetConfig
import net.rsprox.shared.SessionMonitor
import net.rsprox.shared.settings.SettingSetStore
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

/** The part of the proxy a session needs. Narrow so the lifecycle can be exercised without a real client. */
internal interface ClientLauncher {
    /** Get the proxy targets a client can be launched for. */
    fun targets(): List<ProxyTargetConfig>

    /** Pick free ports and bind the HTTP server of [target]. Throws when the target cannot be prepared. */
    fun reserve(target: ProxyTargetConfig): Reservation

    /** Kill the client on [proxyPort], if any, and release the proxy state held for it. Idempotent. */
    fun kill(proxyPort: Int)
}

/** The ports that were reserved for one launch, with the call that performs the launch. */
internal class Reservation(
    /** The port the client reaches the proxy on. */
    val proxyPort: Int,
    /** The port the client fetches its configuration from. */
    val httpPort: Int,
    /** The call that starts the client and blocks until its launcher has completed the handshake. Throws on failure. */
    val launch: (monitor: SessionMonitor<BinaryHeader>) -> Unit,
    /**
     * The check of whether every process that [launch] forked has exited. False until one has been seen.
     * Asked again and again while [launch] blocks, always by the same thread.
     */
    val launcherExited: () -> Boolean,
)

/**
 * Creates the sessions and feeds what happens to a client into its session. It launches and stops the
 * clients of the launched sessions, and attaches a session to each client that something else launched.
 */
public class SessionManager internal constructor(
    /** The part of the proxy that launches and kills clients. */
    private val launcher: ClientLauncher,
    /** The settings that the packets of every session are formatted with. */
    private val settings: SettingSetStore,
    /** The hub that the launched clients dial. */
    private val bridge: BridgeHub,
    /** The longest wait for a launcher to complete its handshake. */
    private val launchTimeoutMs: Long = DEFAULT_LAUNCH_TIMEOUT_MS,
    /** The longest wait for the plugin of a launched client to say hello. */
    private val helloTimeoutMs: Long = DEFAULT_HELLO_TIMEOUT_MS,
) {
    /** The sessions in the order they were first launched or attached. Added to under its own monitor. */
    private val sessions = CopyOnWriteArrayList<Session>()

    /** The lock that makes launches and stops take turns, since the proxy's launch path is not re-entrant. */
    private val launchLock = Any()

    /** Whether a launch never returned, after which the proxy cannot launch again. Guarded by [launchLock]. */
    private var hungLaunch = false

    /** The timer that stops each launch whose plugin never says hello. */
    private val deadlines =
        Executors.newSingleThreadScheduledExecutor { task ->
            Thread(task, "mcp-hello-deadline").apply { isDaemon = true }
        }

    /**
     * Create a session, or relaunch a stopped one. Calling it again for a session that is launching
     * or connected launches nothing and only waits, so a caller can extend a wait that elapsed.
     */
    public fun start(
        target: String?,
        session: String?,
        waitMs: Long,
    ): SessionSnapshot {
        val started =
            synchronized(launchLock) {
                val existing = session?.let { launched(resolve(it), "session_start") }
                if (existing != null) requireTarget(existing, target)

                if (existing == null || existing.client is ClientState.Stopped) {
                    launch(existing, existing?.target ?: resolveTarget(target))
                } else {
                    existing
                }
            }

        started.awaitConnected(waitMs)

        return started.snapshot()
    }

    /**
     * Kill the client of a launched session. The session and its packets stay listed and readable. Idempotent.
     * Throws [ToolError] for an attached session, whose client is not the MCP server's to kill.
     */
    public fun stop(session: String?): SessionSnapshot {
        val resolved = launched(resolve(session), "session_stop")

        synchronized(launchLock) {
            val state = resolved.client

            // Stopped first, so the close of the link below is not mistaken for the client exiting.
            resolved.apply(SessionEvent.Stop("stopped by caller"))

            when (state) {
                is ClientState.Stopped -> {
                    //
                }
                is ClientState.Launching -> release(state.launch)
                is ClientState.Connected -> {
                    state.link.close()
                    launcher.kill(state.launch.proxyPort)
                }
            }
        }

        return resolved.snapshot()
    }

    /**
     * Create the session of a client that something other than this manager is launching on [proxyPort],
     * and return the monitor that records its packets. Returns null for a launch of this manager's own,
     * which has its session already.
     */
    internal fun attach(
        proxyPort: Int,
        target: ProxyTargetConfig,
    ): SessionMonitor<BinaryHeader>? {
        if (launchedSessions().any { it.client.proxyPort() == proxyPort }) return null

        val session = register { id -> AttachedSession(id, target, proxyPort) }

        return tap(session)
    }

    /** End the session that is attached to the client on [proxyPort], if any. Idempotent. */
    internal fun detach(proxyPort: Int) {
        val attached = sessions.filterIsInstance<AttachedSession>().firstOrNull { it.proxyPort == proxyPort }

        attached?.end("the client was closed")
    }

    /** Get a snapshot of every session. */
    public fun list(): List<SessionSnapshot> = sessions.map { it.snapshot() }

    /**
     * Get the session with the given id. A null [ref] means the only session.
     * Throws [ToolError] when no session matches, or when [ref] is null and several exist.
     */
    public fun resolve(ref: String?): Session {
        if (ref != null) {
            return sessions.firstOrNull { it.id.value == ref }
                ?: throw ToolError("no session '$ref'. Sessions: ${sessionIds()}")
        }

        if (sessions.isEmpty()) {
            throw ToolError(
                "no session exists yet; call session_start, or launch a client from the rsprox GUI when it " +
                    "serves this endpoint",
            )
        }

        return sessions.singleOrNull() ?: throw ToolError("several sessions exist; pass one of: ${sessionIds()}")
    }

    /** Get the names of the proxy targets. */
    public fun targets(): List<String> = launcher.targets().map { it.name }

    /** Build the monitor that records the packets and the logins of the client of the session. */
    private fun tap(session: Session): PacketTap =
        PacketTap(session.packets, session.logins, settings, session.target.binaryFolder)

    /** Get the launched sessions. */
    private fun launchedSessions(): List<LaunchedSession> = sessions.filterIsInstance<LaunchedSession>()

    /**
     * Get the session as a launched one, for a tool that needs the client to be the MCP server's own.
     * Throws [ToolError] for an attached session.
     */
    private fun launched(
        session: Session,
        tool: String,
    ): LaunchedSession =
        when (session) {
            is LaunchedSession -> session
            is AttachedSession -> throw ToolError(session.notAvailable(tool))
        }

    /**
     * Make a session under the next id and list it. Launches and attaches number their sessions through
     * here, and not under [launchLock], which a launch holds while the proxy calls back into [attach].
     */
    private fun <S : Session> register(create: (SessionId) -> S): S =
        synchronized(sessions) {
            create(SessionId("s${sessions.size + 1}")).also { sessions += it }
        }

    /** List the session ids for an error message. */
    private fun sessionIds(): String = sessions.joinToString(", ") { it.id.value }.ifEmpty { "none" }

    /** Throw a [ToolError] when the caller names a target other than the one the session belongs to. */
    private fun requireTarget(
        session: LaunchedSession,
        target: String?,
    ) {
        if (target == null || session.target.name.equals(target, ignoreCase = true)) return

        throw ToolError("session ${session.id} belongs to target '${session.target.name}'")
    }

    /** Get the target with the given name, or the first custom target for null. Throws [ToolError] for no match. */
    private fun resolveTarget(name: String?): ProxyTargetConfig {
        val targets = launcher.targets()

        if (name == null) {
            // Target 0 is the official game; a custom target is what a headless caller is here to test.
            return targets.firstOrNull { it.id != 0 } ?: targets.first()
        }

        return targets.firstOrNull { it.name.equals(name, ignoreCase = true) }
            ?: throw ToolError("no target '$name'. Targets: ${targets.joinToString(", ") { it.name }}")
    }

    /**
     * Launch a client of the target for [stopped], or for a new session when it is null, and wait until its
     * launcher has completed the handshake. A new session is listed once the ports of its launch are reserved.
     * Throws [ToolError] when the launch fails or hangs. Must be called with [launchLock] held.
     */
    private fun launch(
        stopped: LaunchedSession?,
        target: ProxyTargetConfig,
    ): LaunchedSession {
        if (hungLaunch) throw ToolError(HUNG_LAUNCH)

        val reservation = reserve(target)
        val session = stopped ?: register { id -> LaunchedSession(id, target) }
        val launch = Launch(session.nextGeneration(), reservation.proxyPort, reservation.httpPort)

        // Registered before the client is forked: its hello can arrive before the launch call returns.
        bridge.expect(launch.httpPort, listener(session, launch))
        session.apply(SessionEvent.Launched(launch))

        val reason = fork(session, reservation)

        if (reason == null) {
            expectHello(session, launch)

            return session
        }

        release(launch)
        session.apply(SessionEvent.Stop(reason))
        throw ToolError("session ${session.id} failed to launch: $reason")
    }

    /** Reserve the ports for a launch of the target. Throws [ToolError] when the target cannot be prepared. */
    private fun reserve(target: ProxyTargetConfig): Reservation =
        try {
            launcher.reserve(target)
        } catch (e: Exception) {
            throw ToolError("could not prepare target '${target.name}': ${rootMessage(e)}")
        }

    /**
     * Fork the client on a thread of its own and wait until its launcher has completed the handshake.
     * Returns the reason when the launch failed or hung, and null when it succeeded.
     * Must be called with [launchLock] held.
     */
    private fun fork(
        session: LaunchedSession,
        reservation: Reservation,
    ): String? {
        // The proxy waits for the launcher's handshake with no timeout, so the wait is bounded from outside.
        val failure = AtomicReference<Throwable?>()
        val thread =
            Thread({
                try {
                    reservation.launch(tap(session))
                } catch (t: Throwable) {
                    failure.set(t)
                }
            }, "mcp-launch-${session.id}")

        thread.isDaemon = true
        thread.start()
        val exited = awaitLaunch(thread, reservation.launcherExited)

        hungLaunch = thread.isAlive

        return when {
            !hungLaunch -> failure.get()?.let(::rootMessage)
            exited -> "the launcher exited before completing its handshake"
            else -> "launcher never completed its handshake"
        }
    }

    /**
     * Wait for the launch thread to end, and determine if the wait was cut short because the launcher exited.
     * The wait also ends, with false, when the launch timeout passes.
     */
    private fun awaitLaunch(
        thread: Thread,
        launcherExited: () -> Boolean,
    ): Boolean {
        val deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(launchTimeoutMs)

        while (thread.isAlive) {
            val remainingMs = TimeUnit.NANOSECONDS.toMillis(deadline - System.nanoTime())
            if (remainingMs <= 0) return false

            if (launcherExited()) {
                // A launcher that exits right after its handshake leaves the launch a moment from returning.
                thread.join(LAUNCHER_EXIT_GRACE_MS)

                return true
            }

            thread.join(minOf(remainingMs, LAUNCH_POLL_MS))
        }

        return false
    }

    /** Give the plugin of the launched client a bounded time to say hello. */
    private fun expectHello(
        session: LaunchedSession,
        launch: Launch,
    ) {
        val overdue = Runnable { abandon(session, launch, NEVER_CONNECTED) }

        deadlines.schedule(overdue, helloTimeoutMs, TimeUnit.MILLISECONDS)
    }

    /** Stop the session for [reason] and kill its client, unless the session has moved on from waiting for it. */
    private fun abandon(
        session: LaunchedSession,
        launch: Launch,
        reason: String,
    ) {
        if (!session.applied(SessionEvent.NeverConnected(launch, reason))) return

        synchronized(launchLock) { release(launch) }
    }

    /** Stop expecting the hello of the launch and kill its client, if any. Must be called with [launchLock] held. */
    private fun release(launch: Launch) {
        bridge.forget(launch.httpPort)
        launcher.kill(launch.proxyPort)
    }

    /** Build the listener that feeds the hello and the close of one launch into its session. */
    private fun listener(
        session: LaunchedSession,
        launch: Launch,
    ): BridgeListener =
        object : BridgeListener {
            /** The id of the session that waits for the client. */
            override val session: String = session.id.value

            /** Connect the session to the link, unless the session has moved on from this launch. */
            override fun onHello(
                link: BridgeLink,
                pid: Long,
            ): Boolean {
                val state = session.apply(SessionEvent.Hello(launch.httpPort, link, pid))

                return state is ClientState.Connected && state.link === link
            }

            /** Stop the session when the link of its connected client closes, and release the proxy state. */
            override fun onClosed(link: BridgeLink) {
                if (!session.applied(SessionEvent.LinkClosed(link))) return

                // The client is gone, or cannot be driven any more. Either way the proxy still holds
                // its process handle and session monitor for the port.
                synchronized(launchLock) { launcher.kill(launch.proxyPort) }
            }

            /** Stop the session with the reason, since the client that was refused dials only once. */
            override fun onRejected(reason: String) {
                abandon(session, launch, "the client started, but its bridge plugin was rejected: $reason")
            }
        }

    /** Get the message of the innermost cause. */
    private fun rootMessage(throwable: Throwable): String {
        val root = generateSequence(throwable) { it.cause }.last()

        return root.message ?: root.toString()
    }

    public companion object {
        /** The time a start waits for the in-client bridge to connect, unless the caller says otherwise. */
        public const val DEFAULT_WAIT_MS: Long = 180_000L

        /**
         * The default of the longest wait for a launcher to complete its handshake.
         * Covers a first run, where the launcher downloads the client before it handshakes.
         */
        private const val DEFAULT_LAUNCH_TIMEOUT_MS = 180_000L

        /** The default of the longest wait for the plugin of a launched client to say hello. */
        private const val DEFAULT_HELLO_TIMEOUT_MS = 60_000L

        /** The pause between two checks of whether the launcher of a blocked launch has exited. */
        private const val LAUNCH_POLL_MS = 100L

        /** The time a launch gets to return after its launcher has exited. */
        private const val LAUNCHER_EXIT_GRACE_MS = 1_000L

        /** The reason of a session whose client never said hello. */
        private const val NEVER_CONNECTED = "the client started, but its bridge plugin never connected"

        /** The refusal of every launch that follows one whose launcher never returned. */
        private const val HUNG_LAUNCH =
            "an earlier launch never completed its handshake; restart the rsprox MCP process"
    }
}
