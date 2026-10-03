package net.rsprox.mcp.packets

import net.rsprox.cache.api.CacheProvider
import net.rsprox.mcp.session.LoginRegistry
import net.rsprox.proxy.binary.BinaryHeader
import net.rsprox.shared.SessionMonitor
import net.rsprox.shared.StreamDirection
import net.rsprox.shared.property.OmitFilteredPropertyTreeFormatter
import net.rsprox.shared.property.PropertyFormatterCollection
import net.rsprox.shared.property.PropertyTreeFormatter
import net.rsprox.shared.property.RootProperty
import net.rsprox.shared.settings.SettingSetStore
import net.rsprox.shared.symbols.SymbolDictionaryProvider

/**
 * The session monitor of one launch. The proxy only delivers callbacks to the per-login instance that
 * [forSession] returns, so the callbacks of this class itself do nothing.
 */
internal class PacketTap(
    private val log: PacketLog,
    private val logins: LoginRegistry,
    private val settings: SettingSetStore,
) : SessionMonitor<BinaryHeader> {
    override fun forSession(header: BinaryHeader): SessionMonitor<BinaryHeader> =
        LoginTap(log, logins, settings, logins.nextEpoch())

    override fun onLogin(header: BinaryHeader) {
        //
    }

    override fun onLogout(header: BinaryHeader) {
        //
    }

    override fun onCacheUpdate(cacheProvider: CacheProvider) {
        //
    }

    override fun onIncomingBytesPerSecondUpdate(bytesPerLastSecond: Long) {
        //
    }

    override fun onOutgoingBytesPerSecondUpdate(bytesPerLastSecond: Long) {
        //
    }

    override fun onNameUpdate(name: String) {
        //
    }

    override fun onUserInformationUpdate(
        userId: Long,
        userHash: Long,
    ) {
        //
    }

    override fun onTranscribe(
        cycle: Int,
        property: RootProperty,
    ) {
        //
    }
}

internal class LoginTap(
    private val log: PacketLog,
    private val logins: LoginRegistry,
    settings: SettingSetStore,
    private val epoch: Int,
) : SessionMonitor<BinaryHeader> {
    // Written on a Netty thread when the transcriber is hooked, read on the transcriber worker.
    @Volatile
    private var cache: CacheProvider? = null

    // The two fields below are only touched on the transcriber worker.
    private var direction = StreamDirection.SERVER_TO_CLIENT
    private var transcribing = false

    private val formatter: PropertyTreeFormatter =
        OmitFilteredPropertyTreeFormatter(
            PropertyFormatterCollection.default(SymbolDictionaryProvider.get(), settings) {
                cache?.get() ?: error("Cache unavailable")
            },
        )

    override fun onCacheUpdate(cacheProvider: CacheProvider) {
        cache = cacheProvider
    }

    override fun onPacketDirection(direction: StreamDirection) {
        this.direction = direction

        // The cache update arrives before the proxy knows whether a decoder exists for the revision,
        // so the first decoded packet is the earliest proof that a transcriber is hooked.
        if (!transcribing) {
            transcribing = true
            logins.update(epoch) { it.copy(transcribing = true) }
        }
    }

    /** Formats on the worker, while the transcriber's session state still matches the packet. */
    override fun onTranscribe(
        cycle: Int,
        property: RootProperty,
    ) {
        val origin = if (direction == StreamDirection.CLIENT_TO_SERVER) Origin.CLIENT else Origin.SERVER
        val text =
            try {
                formatter.format(property).joinToString("\n")
            } catch (e: Exception) {
                // An exception here would unwind into the proxy's transcriber worker.
                "[${property.prot.lowercase()}] <unformattable: $e>"
            }

        log.append(epoch, cycle, origin, property.prot.uppercase(), text)
    }

    override fun onLogin(header: BinaryHeader) {
        logins.login(epoch, header.revision, header.worldId)
        log.append(epoch, 0, Origin.PROXY, "LOGIN", "revision=${header.revision} world=${header.worldId}")
    }

    override fun onLogout(header: BinaryHeader) {
        logins.update(epoch) { it.copy(online = false) }
        log.append(epoch, 0, Origin.PROXY, "LOGOUT", "world=${header.worldId}")
    }

    override fun onNameUpdate(name: String) {
        logins.update(epoch) { it.copy(name = name) }
    }

    override fun onUserInformationUpdate(
        userId: Long,
        userHash: Long,
    ) {
        //
    }

    override fun onIncomingBytesPerSecondUpdate(bytesPerLastSecond: Long) {
        //
    }

    override fun onOutgoingBytesPerSecondUpdate(bytesPerLastSecond: Long) {
        //
    }
}
