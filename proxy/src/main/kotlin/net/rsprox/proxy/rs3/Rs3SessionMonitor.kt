package net.rsprox.proxy.rs3

import net.rsprox.cache.api.CacheProvider
import net.rsprox.cache.api.rs3.Rs3PacketDefinitions
import net.rsprox.cache.api.type.ClientScriptDefinitionProvider
import net.rsprox.shared.SessionMonitor
import net.rsprox.shared.property.ChildProperty
import net.rsprox.shared.property.RootProperty
import net.rsprox.shared.property.int

public class Rs3SessionMonitor(
    private val nanoTime: () -> Long = System::nanoTime,
) : SessionMonitor<Unit> {
    public data class Status(
        public val world: Boolean,
        public val endpoint: Int,
        public val name: String,
    )

    public data class State(
        public val status: Status? = null,
        public val incomingBytesPerSecond: Long = -1,
        public val outgoingBytesPerSecond: Long = -1,
        public val userId: Long = -1,
        public val userHash: Long = -1,
        public val cacheProvider: CacheProvider? = null,
        public val packetDefinitions: Rs3PacketDefinitions? = null,
        public val clientScripts: ClientScriptDefinitionProvider = ClientScriptDefinitionProvider.EMPTY,
    )

    private data class Connection(
        val status: Status,
        val userId: Long,
        val userHash: Long,
    )

    public var stateListener: ((State) -> Unit)? = null
    public var listener: ((cycle: Int, property: RootProperty) -> Unit)? = null

    @Volatile
    public var state: State = State()
        private set

    private val connections = linkedMapOf<Any, Connection>()
    private var lastName: String = ""
    private var incomingBytes = 0L
    private var outgoingBytes = 0L
    private var lastBandwidthUpdate = nanoTime()

    /** Lobby and game sockets overlap; a superseded socket must not clear the current session. */
    @Synchronized
    public fun onConnectionLogin(
        connection: Any,
        world: Boolean,
        endpoint: Int,
        name: String? = null,
        userId: Long = -1,
        userHash: Long = -1,
    ) {
        if (!world) lastName = name.orEmpty()
        connections.entries.removeIf { it.value.status.world == world }
        connections[connection] = Connection(Status(world, endpoint, name ?: lastName), userId, userHash)
        publishConnection()
    }

    @Synchronized
    public fun onConnectionNameUpdate(
        connection: Any,
        name: String,
    ) {
        val current = connections[connection] ?: return
        if (current.userId != state.userId || current.userHash != state.userHash) return
        onNameUpdate(name)
    }

    @Synchronized
    public fun onConnectionClosed(connection: Any) {
        if (connections.remove(connection) != null) publishConnection()
    }

    private fun publishConnection() {
        val current = connections.values.lastOrNull { it.status.world } ?: connections.values.lastOrNull()
        if (current == null) {
            onLogout(Unit)
            return
        }
        val starting = state.status == null
        update(state.copy(status = current.status, userId = current.userId, userHash = current.userHash))
        if (starting) onLogin(Unit)
    }

    /** Count each post-login stream chunk once, independently of decoding, filters and recording success. */
    @Synchronized
    public fun onBytes(
        connection: Any,
        incoming: Boolean,
        count: Int,
    ) {
        require(count >= 0)
        if (connection !in connections) return
        if (incoming) incomingBytes += count else outgoingBytes += count
    }

    @Synchronized
    public fun updateBandwidth() {
        if (state.status == null) return
        val now = nanoTime()
        val elapsed = now - lastBandwidthUpdate
        if (elapsed < 1_000_000_000L) return
        onIncomingBytesPerSecondUpdate((incomingBytes * (1_000_000_000.0 / elapsed)).toLong())
        onOutgoingBytesPerSecondUpdate((outgoingBytes * (1_000_000_000.0 / elapsed)).toLong())
        incomingBytes = 0
        outgoingBytes = 0
        lastBandwidthUpdate = now
    }

    public fun onGameLogin(
        world: Int,
        localPlayerIndex: Int,
    ) {
        val marker =
            object : RootProperty {
                override val prot: String = "LOBBY_TRANSFER"
                override val children: MutableList<ChildProperty<*>> = mutableListOf()
            }
        marker.int("world", world)
        marker.int("playerindex", localPlayerIndex)
        onTranscribe(0, marker)
    }

    @Synchronized
    override fun onLogin(header: Unit) {
        incomingBytes = 0
        outgoingBytes = 0
        lastBandwidthUpdate = nanoTime()
        update(state.copy(incomingBytesPerSecond = 0, outgoingBytesPerSecond = 0))
    }

    @Synchronized
    override fun onLogout(header: Unit) {
        connections.clear()
        lastName = ""
        incomingBytes = 0
        outgoingBytes = 0
        update(
            State(
                cacheProvider = state.cacheProvider,
                packetDefinitions = state.packetDefinitions,
                clientScripts = state.clientScripts,
            ),
        )
    }

    @Synchronized
    override fun onCacheUpdate(cacheProvider: CacheProvider) {
        update(state.copy(cacheProvider = cacheProvider))
    }

    /** RS3 decoder metadata is not an OSRS Cache; publish the actual immutable snapshot. */
    @Synchronized
    public fun onPacketDefinitionsUpdate(definitions: Rs3PacketDefinitions) {
        update(state.copy(packetDefinitions = definitions))
    }

    @Synchronized
    public fun onClientScriptsUpdate(clientScripts: ClientScriptDefinitionProvider) {
        update(state.copy(clientScripts = clientScripts))
    }

    @Synchronized
    override fun onIncomingBytesPerSecondUpdate(bytesPerLastSecond: Long) {
        require(bytesPerLastSecond >= -1)
        update(state.copy(incomingBytesPerSecond = bytesPerLastSecond))
    }

    @Synchronized
    override fun onOutgoingBytesPerSecondUpdate(bytesPerLastSecond: Long) {
        require(bytesPerLastSecond >= -1)
        update(state.copy(outgoingBytesPerSecond = bytesPerLastSecond))
    }

    @Synchronized
    override fun onNameUpdate(name: String) {
        if (name.isBlank() || state.status?.name == name) return
        lastName = name
        connections.replaceAll { _, entry ->
            if (entry.userId == state.userId && entry.userHash == state.userHash) {
                entry.copy(status = entry.status.copy(name = name))
            } else {
                entry
            }
        }
        update(state.copy(status = state.status?.copy(name = name)))
    }

    @Synchronized
    override fun onUserInformationUpdate(
        userId: Long,
        userHash: Long,
    ) {
        update(state.copy(userId = userId, userHash = userHash))
    }

    private fun update(next: State) {
        if (next == state) return
        state = next
        // Queue GUI events in order even when lobby/game sockets use different Netty event loops.
        stateListener?.invoke(next)
    }

    override fun onTranscribe(
        cycle: Int,
        property: RootProperty,
    ) {
        listener?.invoke(cycle, property)
    }
}
