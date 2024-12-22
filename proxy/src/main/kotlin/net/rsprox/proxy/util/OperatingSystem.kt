package net.rsprox.proxy.util

import net.rsprox.patch.NativeClientType

public enum class OperatingSystem(
    public val shortName: String,
) {
    WINDOWS("Win"),
    MAC("Mac"),
    UNIX("Unix"),
    SOLARIS("Sol"),
    ;

    public fun toNativeClientType(): NativeClientType = when (this) {
        WINDOWS -> NativeClientType.WIN
        MAC -> NativeClientType.MAC
        else -> throw UnsupportedOperationException("Unknown native client type: $this")
    }
}
