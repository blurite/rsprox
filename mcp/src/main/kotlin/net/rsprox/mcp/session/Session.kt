package net.rsprox.mcp.session

import com.fasterxml.jackson.annotation.JsonInclude
import net.rsprox.mcp.bridge.BridgeLink
import net.rsprox.mcp.packets.Origin
import net.rsprox.mcp.packets.PacketLog
import net.rsprox.mcp.server.ToolError
import net.rsprox.proxy.target.ProxyTargetConfig
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/** Stable for the life of the MCP process. Survives client restarts. Never a port. */
@JvmInline
public value class SessionId(
    public val value: String,
) {
    override fun toString(): String = value
}

/** One client process generation of a session. A restart makes a new launch on new ports. */
public data class Launch(
    val generation: Int,
    val proxyPort: Int,
    /** The identity the in-client bridge reports when it connects. */
    val httpPort: Int,
    val startedAtMs: Long,
)

public sealed interface ClientState {
    public data class Stopped(
        val reason: String,
    ) : ClientState

    public data class Launching(
        val launch: Launch,
    ) : ClientState

    public data class Connected(
        val launch: Launch,
        val link: BridgeLink,
        val pid: Long,
    ) : ClientState
}

/** Everything that can change a session's [ClientState]. Several threads raise these; [reduce] decides. */
public sealed interface SessionEvent {
    public data class Launched(
        val launch: Launch,
    ) : SessionEvent

    public data class Hello(
        val httpPort: Int,
        val link: BridgeLink,
        val pid: Long,
    ) : SessionEvent

    public data class LinkClosed(
        val link: BridgeLink,
    ) : SessionEvent

    public data class Stop(
        val reason: String,
    ) : SessionEvent
}

/** Returns [state] itself when the event is stale or does not apply. */
internal fun reduce(
    state: ClientState,
    event: SessionEvent,
): ClientState =
    when (event) {
        is SessionEvent.Launched ->
            if (state is ClientState.Stopped) ClientState.Launching(event.launch) else state
        is SessionEvent.Hello ->
            if (state is ClientState.Launching && state.launch.httpPort == event.httpPort) {
                ClientState.Connected(state.launch, event.link, event.pid)
            } else {
                state
            }
        is SessionEvent.LinkClosed ->
            if (state is ClientState.Connected && state.link === event.link) {
                ClientState.Stopped("client exited")
            } else {
                state
            }
        is SessionEvent.Stop ->
            if (state is ClientState.Stopped) state else ClientState.Stopped(event.reason)
    }

public data class LoginInfo(
    val epoch: Int,
    val revision: Int,
    val world: Int,
    val name: String?,
    val online: Boolean,
    /** False while online means no decoder was hooked for this login, so its packets never reach the log. */
    val transcribing: Boolean,
)

/** The newest login of a session. Updates from an older login are ignored. */
internal class LoginRegistry {
    private var epoch = 0
    private var info: LoginInfo? = null

    @Synchronized
    fun nextEpoch(): Int = ++epoch

    @Synchronized
    fun current(): LoginInfo? = info

    @Synchronized
    fun login(
        epoch: Int,
        revision: Int,
        world: Int,
    ) {
        val current = info
        if (current != null && current.epoch > epoch) return
        info =
            if (current != null && current.epoch == epoch) {
                current.copy(online = true)
            } else {
                LoginInfo(epoch, revision, world, name = null, online = true, transcribing = false)
            }
    }

    @Synchronized
    fun update(
        epoch: Int,
        change: (LoginInfo) -> LoginInfo,
    ) {
        val current = info ?: return
        if (current.epoch == epoch) info = change(current)
    }
}

/** What the session tools return. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public data class SessionSnapshot(
    val session: String,
    val target: String,
    /** `stopped`, `launching` or `connected`. */
    val state: String,
    val reason: String?,
    val generation: Int?,
    val proxyPort: Int?,
    val httpPort: Int?,
    val pid: Long?,
    val login: LoginInfo?,
    val cursor: Long,
)

public class Session internal constructor(
    public val id: SessionId,
    public val target: ProxyTargetConfig,
) {
    /** Owned by the session rather than a launch, so a restart keeps the records and the cursor space. */
    public val packets: PacketLog = PacketLog()
    internal val logins = LoginRegistry()
    private val lock = ReentrantLock()
    private val changed = lock.newCondition()
    private var generations = 0

    @Volatile
    public var client: ClientState = ClientState.Stopped("not started")
        private set

    internal fun nextGeneration(): Int = lock.withLock { ++generations }

    /** The only writer of [client]. Every real transition leaves a marker in the packet log. */
    internal fun apply(event: SessionEvent): ClientState =
        lock.withLock {
            val before = client
            val after = reduce(before, event)
            if (after !== before) {
                client = after
                mark(after)
                changed.signalAll()
            }
            after
        }

    /** Blocks while the client is launching, for at most [waitMs]. Never throws on timeout. */
    internal fun awaitConnected(waitMs: Long): ClientState =
        lock.withLock {
            var remaining = TimeUnit.MILLISECONDS.toNanos(waitMs)
            while (client is ClientState.Launching && remaining > 0) {
                remaining = changed.awaitNanos(remaining)
            }
            client
        }

    /** The link of the connected client, or a [ToolError] that says what to do about its absence. */
    internal fun requireLink(): BridgeLink =
        when (val state = client) {
            is ClientState.Connected -> state.link
            is ClientState.Launching ->
                throw ToolError("session $id is still launching; call session_start with this session to wait for it")
            is ClientState.Stopped ->
                throw ToolError("session $id has no connected client: ${state.reason}")
        }

    public fun snapshot(): SessionSnapshot {
        val state = client
        val launch =
            when (state) {
                is ClientState.Stopped -> null
                is ClientState.Launching -> state.launch
                is ClientState.Connected -> state.launch
            }
        return SessionSnapshot(
            session = id.value,
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

    private fun mark(state: ClientState) {
        val (prot, text) =
            when (state) {
                is ClientState.Launching ->
                    "CLIENT_LAUNCHED" to
                        "generation=${state.launch.generation} proxyPort=${state.launch.proxyPort} " +
                        "httpPort=${state.launch.httpPort}"
                is ClientState.Connected -> "CLIENT_CONNECTED" to "pid=${state.pid}"
                is ClientState.Stopped -> "CLIENT_EXITED" to state.reason
            }
        packets.append(logins.current()?.epoch ?: 0, 0, Origin.PROXY, prot, text)
    }
}
