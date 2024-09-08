package net.rsprox.proxy.util

import net.rsprox.proxy.binary.BinaryHeader
import net.rsprox.shared.SessionMonitor
import net.rsprox.shared.property.RootProperty

public object NopSessionMonitor : SessionMonitor<BinaryHeader> {
    override fun onLogin(header: BinaryHeader) {
    }

    override fun onLogout(header: BinaryHeader) {
    }

    override fun onIncomingBytesPerSecondUpdate(bytesPerLastSecond: Long) {
    }

    override fun onOutgoingBytesPerSecondUpdate(bytesPerLastSecond: Long) {
    }

    override fun onNameUpdate(name: String) {
    }

    override fun onUserInformationUpdate(
        userId: Long,
        userHash: Long,
    ) {
    }

    override fun onTranscribe(
        cycle: Int,
        property: RootProperty,
    ) {
    }
}
