package net.rsprox.shared

import net.rsprox.cache.api.CacheProvider
import net.rsprox.shared.property.RootProperty

public interface SessionMonitor<T> {
    /** Bind delayed callbacks to one recording rather than the latest active login. */
    public fun forSession(header: T): SessionMonitor<T> = this

    /** Called on the transcriber worker before the properties of each packet are published. */
    public fun onPacketDirection(direction: StreamDirection) {}

    public fun onLogin(header: T)

    public fun onLogout(header: T)

    public fun onCacheUpdate(cacheProvider: CacheProvider)

    public fun onIncomingBytesPerSecondUpdate(bytesPerLastSecond: Long)

    public fun onOutgoingBytesPerSecondUpdate(bytesPerLastSecond: Long)

    public fun onNameUpdate(name: String)

    public fun onUserInformationUpdate(
        userId: Long,
        userHash: Long,
    )

    public fun onTranscribe(
        cycle: Int,
        property: RootProperty,
    )
}
