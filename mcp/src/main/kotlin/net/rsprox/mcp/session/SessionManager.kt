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
import java.util.concurrent.atomic.AtomicReference

/** The part of the proxy a session needs. Narrow so the lifecycle can be exercised without a real client. */
internal interface ClientLauncher {
    fun targets(): List<ProxyTargetConfig>

    /** Picks free ports and binds the HTTP server of [target]. Throws when the target cannot be prepared. */
    fun reserve(target: ProxyTargetConfig): Reservation

    /** Kills the client on [proxyPort], if any, and releases the proxy state held for it. Idempotent. */
    fun kill(proxyPort: Int)
}

internal class Reservation(
    val proxyPort: Int,
    val httpPort: Int,
    /** Starts the client and blocks until its launcher has completed the handshake. Throws on failure. */
    val launch: (monitor: SessionMonitor<BinaryHeader>) -> Unit,
)

public class SessionManager internal constructor(
    private val launcher: ClientLauncher,
    private val settings: SettingSetStore,
    private val bridge: BridgeHub,
    private val launchTimeoutMs: Long = DEFAULT_LAUNCH_TIMEOUT_MS,
) {
    private val sessions = CopyOnWriteArrayList<Session>()

    // The proxy's launch path is not re-entrant, so launches and stops take turns.
    private val launchLock = Any()
    private var hungLaunch = false

    /**
     * Creates a session, or relaunches a stopped one. Calling it again for a session that is launching
     * or connected launches nothing and only waits, so a caller can extend a wait that elapsed.
     */
    public fun start(
        target: String?,
        session: String?,
        waitMs: Long,
    ): SessionSnapshot {
        val started =
            synchronized(launchLock) {
                val existing = session?.let { resolve(it) }
                if (existing != null && target != null && !existing.target.name.equals(target, ignoreCase = true)) {
                    throw ToolError("session ${existing.id} belongs to target '${existing.target.name}'")
                }
                val current = existing ?: Session(SessionId("s${sessions.size + 1}"), resolveTarget(target))
                if (current.client is ClientState.Stopped) launch(current)
                current
            }
        started.awaitConnected(waitMs)
        return started.snapshot()
    }

    /** Kills the client. The session and its packets stay listed and readable. Idempotent. */
    public fun stop(session: String?): SessionSnapshot {
        val resolved = resolve(session)
        synchronized(launchLock) {
            val state = resolved.client
            // Stopped first, so the close of the link below is not mistaken for the client exiting.
            resolved.apply(SessionEvent.Stop("stopped by caller"))
            when (state) {
                is ClientState.Stopped -> {}
                is ClientState.Launching -> launcher.kill(state.launch.proxyPort)
                is ClientState.Connected -> {
                    state.link.close()
                    launcher.kill(state.launch.proxyPort)
                }
            }
        }
        return resolved.snapshot()
    }

    public fun list(): List<SessionSnapshot> = sessions.map { it.snapshot() }

    /** A null [ref] means the only session. */
    public fun resolve(ref: String?): Session {
        if (ref != null) {
            return sessions.firstOrNull { it.id.value == ref }
                ?: throw ToolError("no session '$ref'. Sessions: ${sessionIds()}")
        }
        return sessions.singleOrNull()
            ?: throw ToolError(
                if (sessions.isEmpty()) {
                    "no session exists yet; call session_start first"
                } else {
                    "several sessions exist; pass one of: ${sessionIds()}"
                },
            )
    }

    public fun targets(): List<String> = launcher.targets().map { it.name }

    private fun sessionIds(): String = sessions.joinToString(", ") { it.id.value }.ifEmpty { "none" }

    private fun resolveTarget(name: String?): ProxyTargetConfig {
        val targets = launcher.targets()
        if (name == null) {
            // Target 0 is the official game; a custom target is what a headless caller is here to test.
            return targets.firstOrNull { it.id != 0 } ?: targets.first()
        }
        return targets.firstOrNull { it.name.equals(name, ignoreCase = true) }
            ?: throw ToolError("no target '$name'. Targets: ${targets.joinToString(", ") { it.name }}")
    }

    private fun launch(session: Session) {
        if (hungLaunch) {
            throw ToolError("an earlier launch never completed its handshake; restart the rsprox MCP process")
        }
        val reservation =
            try {
                launcher.reserve(session.target)
            } catch (e: Exception) {
                throw ToolError("could not prepare target '${session.target.name}': ${rootMessage(e)}")
            }
        val launch =
            Launch(session.nextGeneration(), reservation.proxyPort, reservation.httpPort, System.currentTimeMillis())
        sessions.addIfAbsent(session)
        // Registered before the client is forked: its hello can arrive before the launch call returns.
        bridge.expect(launch.httpPort, listener(session, launch))
        session.apply(SessionEvent.Launched(launch))

        // The proxy waits for the launcher's handshake with no timeout, so the wait is bounded from outside.
        val failure = AtomicReference<Throwable?>()
        val thread =
            Thread({
                try {
                    reservation.launch(PacketTap(session.packets, session.logins, settings))
                } catch (t: Throwable) {
                    failure.set(t)
                }
            }, "mcp-launch-${session.id}")
        thread.isDaemon = true
        thread.start()
        thread.join(launchTimeoutMs)
        val reason =
            if (thread.isAlive) {
                hungLaunch = true
                "launcher never completed its handshake"
            } else {
                failure.get()?.let(::rootMessage) ?: return
            }
        launcher.kill(launch.proxyPort)
        session.apply(SessionEvent.Stop(reason))
        throw ToolError("session ${session.id} failed to launch: $reason")
    }

    private fun listener(
        session: Session,
        launch: Launch,
    ): BridgeListener =
        object : BridgeListener {
            override val session: String = session.id.value

            override fun onHello(
                link: BridgeLink,
                pid: Long,
            ): Boolean {
                val state = session.apply(SessionEvent.Hello(launch.httpPort, link, pid))
                return state is ClientState.Connected && state.link === link
            }

            override fun onClosed(link: BridgeLink) {
                val before = session.client
                if (session.apply(SessionEvent.LinkClosed(link)) === before) return
                // The client is gone, or cannot be driven any more. Either way the proxy still holds
                // its process handle and session monitor for the port.
                synchronized(launchLock) { launcher.kill(launch.proxyPort) }
            }
        }

    private fun rootMessage(throwable: Throwable): String {
        val root = generateSequence(throwable) { it.cause }.last()
        return root.message ?: root.toString()
    }

    public companion object {
        /** How long a start waits for the in-client bridge to connect, unless the caller says otherwise. */
        public const val DEFAULT_WAIT_MS: Long = 180_000L

        // Covers a first run, where the launcher downloads the client before it handshakes.
        private const val DEFAULT_LAUNCH_TIMEOUT_MS = 180_000L
    }
}
