package net.rsprox.mcp.session

import com.fasterxml.jackson.annotation.JsonInclude
import net.rsprox.mcp.bridge.BridgeLink
import net.rsprox.mcp.packets.Origin
import net.rsprox.mcp.packets.PacketLog
import net.rsprox.mcp.server.ToolError
import net.rsprox.proxy.binary.BinaryHeader
import net.rsprox.proxy.target.ProxyTargetConfig
import java.time.Instant
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/** Stable for the life of the MCP process. Survives client restarts. Never a port. */
@JvmInline
public value class SessionId(
    /** The id as callers write it, such as `s1`. */
    public val value: String,
) {
    /** Get the id as callers write it. */
    override fun toString(): String = value
}

/** One client process generation of a session. A restart makes a new launch on new ports. */
public data class Launch(
    /** The number of this launch within its session, starting at 1. */
    val generation: Int,
    /** The port the client reaches the proxy on. */
    val proxyPort: Int,
    /** The identity the in-client bridge reports when it connects. */
    val httpPort: Int,
)

/** Whether a launched session has a client, and how far that client has come. */
public sealed interface ClientState {
    /** The launch of the client, or null when the state has no client. */
    public val launch: Launch?

    /** The session has no client. */
    public data class Stopped(
        /** The reason the session has no client. */
        val reason: String,
    ) : ClientState {
        /** Null, since the session has no client. */
        override val launch: Launch? get() = null
    }

    /** The client was launched and its plugin has not said hello yet. */
    public data class Launching(
        /** The launch whose client has not connected yet. */
        override val launch: Launch,
    ) : ClientState

    /** The plugin in the client is connected, so the client can be driven. */
    public data class Connected(
        /** The launch that the client belongs to. */
        override val launch: Launch,
        /** The link to the plugin in the client. */
        val link: BridgeLink,
        /** The process id of the client. */
        val pid: Long,
    ) : ClientState
}

/** One login of a session, as the proxy reports it. A value that is not known yet is left out of the JSON. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public data class LoginInfo(
    /** The number of this login within its session, starting at 1. */
    val epoch: Int,
    /** The game revision of the client. */
    val revision: Int,
    /** The world that was logged in to. */
    val world: Int,
    /** The host of that world. */
    val host: String,
    /** The index of the local player in that world. */
    val localPlayerIndex: Int,
    /** The moment the server accepted the login, in ISO-8601. */
    val connectedAt: String,
    /**
     * The file the login is recorded to, relative to the `binary` directory of rsprox.
     * Null for a target that is not recorded.
     */
    val captureFile: String?,
    /** The display name of the player, or null until the proxy reports it. */
    val name: String?,
    /** Whether the login is still in the game. */
    val online: Boolean,
    /**
     * Whether a packet of this login has been decoded.
     * False while online means no decoder was hooked for this login, so its packets never reach the log.
     */
    val transcribing: Boolean,
    /** The newest server tick that a packet was decoded in, or null before the first packet. */
    val tick: Int?,
) {
    internal companion object {
        /** Build the login of the given epoch from what the proxy knows the moment the server accepts it. */
        internal fun of(
            epoch: Int,
            header: BinaryHeader,
            captureFile: String?,
        ): LoginInfo =
            LoginInfo(
                epoch = epoch,
                revision = header.revision,
                world = header.worldId,
                host = header.worldHost,
                localPlayerIndex = header.localPlayerIndex,
                connectedAt = Instant.ofEpochMilli(header.timestamp).toString(),
                captureFile = captureFile,
                name = null,
                online = true,
                transcribing = false,
                tick = null,
            )
    }
}

/** The newest login of a session. Updates from an older login are ignored. */
internal class LoginRegistry {
    /** The epoch handed to the newest login. */
    private var epoch = 0

    /** The newest login, or null before the first one. */
    private var info: LoginInfo? = null

    /** Get the epoch for a login that is about to begin. */
    @Synchronized
    fun nextEpoch(): Int = ++epoch

    /** Get the newest login, or null before the first one. */
    @Synchronized
    fun current(): LoginInfo? = info

    /**
     * Register that the login is in the game, unless a newer login is known.
     * A login that is known already, which the proxy reports again when it reconnects, only goes back online.
     */
    @Synchronized
    fun login(login: LoginInfo) {
        val current = info
        if (current != null && current.epoch > login.epoch) return

        info = if (current != null && current.epoch == login.epoch) current.copy(online = true) else login
    }

    /** Apply the change to the newest login when it is the one of the given epoch. */
    @Synchronized
    fun update(
        epoch: Int,
        change: (LoginInfo) -> LoginInfo,
    ) {
        val current = info ?: return
        if (current.epoch == epoch) info = change(current)
    }
}

/** What the session tools return. A value that does not apply, or is not known yet, is left out of the JSON. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public data class SessionSnapshot(
    /** The id of the session. */
    val session: String,
    /** Who owns the client: `launched` when the MCP server started it, `attached` when the rsprox GUI did. */
    val kind: String,
    /** The name of the proxy target the session belongs to. */
    val target: String,
    /** The state: `stopped`, `launching` or `connected` when launched, `attached` or `ended` when attached. */
    val state: String,
    /** The reason the session has no client, or null while it has one. */
    val reason: String?,
    /** The number of the current launch, or null while stopped and for an attached session. */
    val generation: Int?,
    /** The proxy port of the client, or null while a launched session is stopped. */
    val proxyPort: Int?,
    /** The HTTP port of the current launch, or null while stopped and for an attached session. */
    val httpPort: Int?,
    /** The process id of the client, or null unless a launched session is connected. */
    val pid: Long?,
    /** The newest login, or null before the first one. */
    val login: LoginInfo?,
    /** The packet cursor of the newest record in the session's log. */
    val cursor: Long,
)

/**
 * One client of a proxy target, with its packet log and its logins.
 * A session is either launched, and its client is the MCP server's to drive and to stop, or attached
 * to a client that the rsprox GUI launched, which the MCP server only reads the packets of.
 */
public sealed class Session(
    /** The id that callers name the session by. */
    public val id: SessionId,
    /** The proxy target that the client of the session is for. */
    public val target: ProxyTargetConfig,
) {
    /** The packet log. Owned by the session, not a launch, so a restart keeps the records and the cursor space. */
    public val packets: PacketLog = PacketLog()

    /** The logins of the session. */
    internal val logins = LoginRegistry()

    /**
     * Get the link to the plugin in the client, for the client tool named [tool].
     * Throws a [ToolError] that says why the tool cannot reach the client.
     */
    internal abstract fun link(tool: String): BridgeLink

    /** Get what the session tools report of the session at this moment. */
    public abstract fun snapshot(): SessionSnapshot

    /** Append a lifecycle marker to the packet log. */
    protected fun mark(
        prot: String,
        text: String,
    ) {
        packets.append(logins.current()?.epoch ?: 0, 0, Origin.PROXY, prot, text)
    }
}

/** A session whose client the MCP server launched, across every launch of that client. */
public class LaunchedSession internal constructor(
    id: SessionId,
    target: ProxyTargetConfig,
) : Session(id, target) {
    /** The lock that makes each state transition and its marker one step. */
    private val lock = ReentrantLock()

    /** The condition that wakes the callers that wait for the client to connect. */
    private val changed = lock.newCondition()

    /** The number of launches so far. */
    private var generations = 0

    /** The state of the client, which only [transition] changes. */
    @Volatile
    public var client: ClientState = ClientState.Stopped("not started")
        private set

    /** Get the generation number for a launch that is about to begin. */
    internal fun nextGeneration(): Int = lock.withLock { ++generations }

    /**
     * Move to the state that [next] makes of the current one, and determine if that changed it.
     * [next] returns the state it is given when what happened is stale or does not apply.
     * The only writer of [client]. Every real transition leaves a marker in the packet log.
     */
    private fun transition(next: (ClientState) -> ClientState): Boolean =
        lock.withLock {
            val before = client
            val after = next(before)

            if (after !== before) {
                client = after
                mark(after)
                changed.signalAll()
            }

            after !== before
        }

    /** Register that [launch] of the client began, unless the session has a client already. */
    internal fun launched(launch: Launch) {
        transition { if (it is ClientState.Stopped) ClientState.Launching(launch) else it }
    }

    /**
     * Connect the session to the plugin that said hello, when the session is launching and [httpPort] is
     * the one of its launch. Determines if the session is connected through [link] afterwards.
     */
    internal fun hello(
        httpPort: Int,
        link: BridgeLink,
        pid: Long,
    ): Boolean =
        lock.withLock {
            transition {
                if (it is ClientState.Launching && it.launch.httpPort == httpPort) {
                    ClientState.Connected(it.launch, link, pid)
                } else {
                    it
                }
            }

            (client as? ClientState.Connected)?.link === link
        }

    /** Stop the session when [link] is the link of its connected client, and determine if that stopped it. */
    internal fun linkClosed(link: BridgeLink): Boolean =
        transition { if (it is ClientState.Connected && it.link === link) ClientState.Stopped("client exited") else it }

    /** Stop the session for [reason], unless it is stopped already. */
    internal fun stop(reason: String) {
        transition { if (it is ClientState.Stopped) it else ClientState.Stopped(reason) }
    }

    /**
     * Stop the session for [reason] when it still waits for the client of [launch], and determine if
     * that stopped it.
     */
    internal fun neverConnected(
        launch: Launch,
        reason: String,
    ): Boolean =
        transition { if (it is ClientState.Launching && it.launch == launch) ClientState.Stopped(reason) else it }

    /** Block while the client is launching, for at most [waitMs]. Never throws on timeout. */
    internal fun awaitConnected(waitMs: Long) {
        lock.withLock {
            var remaining = TimeUnit.MILLISECONDS.toNanos(waitMs)
            while (client is ClientState.Launching && remaining > 0) {
                remaining = changed.awaitNanos(remaining)
            }
        }
    }

    /** Get the link of the connected client. Throws a [ToolError] that says what to do about its absence. */
    override fun link(tool: String): BridgeLink =
        when (val state = client) {
            is ClientState.Connected -> state.link
            is ClientState.Launching ->
                throw ToolError("session $id is still launching; call session_start with this session to wait for it")
            is ClientState.Stopped ->
                throw ToolError("session $id has no connected client: ${state.reason}")
        }

    /** Get what the session tools report of the session at this moment. */
    override fun snapshot(): SessionSnapshot {
        val state = client
        val launch = state.launch

        return SessionSnapshot(
            session = id.value,
            kind = "launched",
            target = target.name,
            state =
                when (state) {
                    is ClientState.Stopped -> "stopped"
                    is ClientState.Launching -> "launching"
                    is ClientState.Connected -> "connected"
                },
            reason = (state as? ClientState.Stopped)?.reason,
            generation = launch?.generation,
            proxyPort = launch?.proxyPort,
            httpPort = launch?.httpPort,
            pid = (state as? ClientState.Connected)?.pid,
            login = logins.current(),
            cursor = packets.head().seq,
        )
    }

    /** Append the lifecycle marker of the state to the packet log. */
    private fun mark(state: ClientState) {
        when (state) {
            is ClientState.Launching ->
                mark(
                    "CLIENT_LAUNCHED",
                    "generation=${state.launch.generation} proxyPort=${state.launch.proxyPort} " +
                        "httpPort=${state.launch.httpPort}",
                )
            is ClientState.Connected -> mark("CLIENT_CONNECTED", "pid=${state.pid}")
            is ClientState.Stopped -> mark("CLIENT_EXITED", state.reason)
        }
    }
}

/**
 * A session of a client that the rsprox GUI launched. The client belongs to whoever runs the GUI, so
 * the MCP server reads its packets and never stops or drives it.
 */
public class AttachedSession internal constructor(
    id: SessionId,
    target: ProxyTargetConfig,
    /** The port the client reaches the proxy on. */
    public val proxyPort: Int,
) : Session(id, target) {
    /** The reason the session ended, or null while the client runs. */
    @Volatile
    private var ended: String? = null

    init {
        mark("CLIENT_ATTACHED", "proxyPort=$proxyPort")
    }

    /** End the session for [reason], since its client is gone. Its packets stay readable. Idempotent. */
    @Synchronized
    internal fun end(reason: String) {
        if (ended != null) return

        ended = reason
        mark("CLIENT_EXITED", reason)
    }

    /** Refuse the tool, since no client tool reaches the client of an attached session. */
    override fun link(tool: String): BridgeLink = throw ToolError(notAvailable(tool))

    /** Build the refusal of a tool that acts on the client, for the error of that tool. */
    internal fun notAvailable(tool: String): String =
        "$tool is not available for an attached session: session $id belongs to a client that was " +
            "launched from the rsprox GUI, so only its packets can be read"

    /** Get what the session tools report of the session at this moment. */
    override fun snapshot(): SessionSnapshot {
        val reason = ended

        return SessionSnapshot(
            session = id.value,
            kind = "attached",
            target = target.name,
            state = if (reason == null) "attached" else "ended",
            reason = reason,
            generation = null,
            proxyPort = proxyPort,
            httpPort = null,
            pid = null,
            login = logins.current(),
            cursor = packets.head().seq,
        )
    }
}
