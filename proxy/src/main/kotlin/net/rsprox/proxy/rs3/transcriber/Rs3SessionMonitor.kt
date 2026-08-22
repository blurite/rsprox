package net.rsprox.proxy.rs3.transcriber

import net.rsprox.shared.SessionMonitor

public class Rs3SessionMonitor : SessionMonitor<Unit> {
    override fun onLogin(header: Unit) {}

    override fun onLogout(header: Unit) {}

    override fun onCacheUpdate(cacheProvider: net.rsprox.cache.api.CacheProvider) {
        // todo
    }

    override fun onIncomingBytesPerSecondUpdate(bytesPerLastSecond: Long) {
        // todo
    }

    override fun onOutgoingBytesPerSecondUpdate(bytesPerLastSecond: Long) {
        // todo
    }

    override fun onNameUpdate(name: String) {
        // todo
    }

    override fun onUserInformationUpdate(
        userId: Long,
        userHash: Long,
    ) {
        // todo
    }

    public var listener: ((cycle: Int, property: net.rsprox.shared.property.RootProperty) -> Unit)? = null

    override fun onTranscribe(
        cycle: Int,
        property: net.rsprox.shared.property.RootProperty,
    ) {
        listener?.invoke(cycle, property)
    }
}
