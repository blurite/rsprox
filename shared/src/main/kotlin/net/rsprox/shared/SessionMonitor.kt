package net.rsprox.shared

import net.rsprox.shared.property.RootProperty

public interface SessionMonitor<T> {
    public fun onLogin(header: T)

    public fun onLogout(header: T)

    public fun onIncomingBytesPerSecondUpdate(bytesPerLastSecond: Long)

    public fun onOutgoingBytesPerSecondUpdate(bytesPerLastSecond: Long)

    public fun onNameUpdate(name: String)

    public fun onTranscribe(
        cycle: Int,
        property: RootProperty<*>,
    )
}
