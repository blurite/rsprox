package net.rsprox.mcp.packets

import net.rsprox.cache.api.CacheProvider
import net.rsprox.mcp.session.LoginInfo
import net.rsprox.mcp.session.LoginRegistry
import net.rsprox.proxy.binary.BinaryHeader
import net.rsprox.proxy.util.NopSessionMonitor
import net.rsprox.shared.SessionMonitor
import net.rsprox.shared.StreamDirection
import net.rsprox.shared.property.OmitFilteredPropertyTreeFormatter
import net.rsprox.shared.property.PropertyFormatterCollection
import net.rsprox.shared.property.PropertyTreeFormatter
import net.rsprox.shared.property.RootProperty
import net.rsprox.shared.settings.SettingSetStore
import net.rsprox.shared.symbols.SymbolDictionaryProvider

/**
 * The session monitor of one client. The proxy only delivers callbacks to the per-login instance that
 * [forSession] returns, so the callbacks of this class itself do nothing.
 */
internal class PacketTap(
    /** The log that the packets of every login go to. */
    private val log: PacketLog,
    /** The registry of the session's logins. */
    private val logins: LoginRegistry,
    /** The settings that the packets are formatted with. */
    private val settings: SettingSetStore,
    /** The folder the proxy records the logins to, under its `binary` directory, or null when it records none. */
    private val captureFolder: String?,
) : SessionMonitor<BinaryHeader> by NopSessionMonitor {
    /** Create the tap of one login, under the next login epoch. */
    override fun forSession(header: BinaryHeader): SessionMonitor<BinaryHeader> =
        LoginTap(log, logins, settings, logins.nextEpoch(), captureFolder?.let { "$it/${header.fileName()}" })
}

/** The session monitor of one login, which appends the packets of that login to the log as text. */
private class LoginTap(
    /** The log that the packets of this login go to. */
    private val log: PacketLog,
    /** The registry that hears what happens to this login. */
    private val logins: LoginRegistry,
    settings: SettingSetStore,
    /** The login epoch that every record of this tap carries. */
    private val epoch: Int,
    /** The file the proxy records this login to, or null when it records none. */
    private val captureFile: String?,
) : SessionMonitor<BinaryHeader> by NopSessionMonitor {
    /**
     * The game cache that the formatter looks names up in, or null before the proxy has handed it over.
     * Written on a Netty thread when the transcriber is hooked, read on the transcriber worker.
     */
    @Volatile
    private var cache: CacheProvider? = null

    /** The direction of the packet being transcribed. Only touched on the transcriber worker. */
    private var direction = StreamDirection.SERVER_TO_CLIENT

    /** Whether a packet of this login has been decoded. Only touched on the transcriber worker. */
    private var transcribing = false

    /** The newest tick that was registered for this login. Only touched on the transcriber worker. */
    private var tick = -1

    /** The formatter that turns a decoded packet into text. */
    private val formatter: PropertyTreeFormatter =
        OmitFilteredPropertyTreeFormatter(
            PropertyFormatterCollection.default(SymbolDictionaryProvider.get(), settings) {
                cache?.get() ?: error("Cache unavailable")
            },
        )

    /** Get this tap, which is the monitor of its one login already. */
    override fun forSession(header: BinaryHeader): SessionMonitor<BinaryHeader> = this

    /** Keep the cache for the formatter, and register the file that the login is recorded to. */
    override fun onCacheUpdate(cacheProvider: CacheProvider) {
        cache = cacheProvider

        logins.update(epoch) { it.copy(captureFile = captureFile) }
    }

    /** Keep the direction of the next packet. The first packet also registers that this login is being transcribed. */
    override fun onPacketDirection(direction: StreamDirection) {
        this.direction = direction

        // The cache update arrives before the proxy knows whether a decoder exists for the revision,
        // so the first decoded packet is the earliest proof that a transcriber is hooked.
        if (!transcribing) {
            transcribing = true
            logins.update(epoch) { it.copy(transcribing = true) }
        }
    }

    /**
     * Append the packet to the log as text, and register its tick when it is a new one.
     * Formats on the worker, while the transcriber's session state still matches the packet.
     */
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

        if (cycle != tick) {
            tick = cycle
            logins.update(epoch) { it.copy(tick = cycle) }
        }
    }

    /** Register the login and mark it in the log. */
    override fun onLogin(header: BinaryHeader) {
        logins.login(LoginInfo.of(epoch, header))
        log.append(epoch, 0, Origin.PROXY, "LOGIN", "revision=${header.revision} world=${header.worldId}")
    }

    /** Register the logout and mark it in the log. */
    override fun onLogout(header: BinaryHeader) {
        logins.update(epoch) { it.copy(online = false) }
        log.append(epoch, 0, Origin.PROXY, "LOGOUT", "world=${header.worldId}")
    }

    /** Register the name of the player that logged in. */
    override fun onNameUpdate(name: String) {
        logins.update(epoch) { it.copy(name = name) }
    }
}
