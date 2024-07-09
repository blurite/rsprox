package net.rsprox.proxy.util

private val OPERATING_SYSTEM: String = System.getProperty("os.name").lowercase()

private fun isWindows(): Boolean {
    return OPERATING_SYSTEM.indexOf("win") >= 0
}

private fun isMac(): Boolean {
    return OPERATING_SYSTEM.indexOf("mac") >= 0
}

private fun isUnix(): Boolean {
    return OPERATING_SYSTEM.indexOf("nix") >= 0 ||
        OPERATING_SYSTEM.indexOf("nux") >= 0 ||
        OPERATING_SYSTEM.indexOf("aix") >= 0
}

private fun isSolaris(): Boolean {
    return OPERATING_SYSTEM.indexOf("sunos") >= 0
}

public fun getOperatingSystem(): OperatingSystem {
    return when {
        isWindows() -> OperatingSystem.WINDOWS
        isMac() -> OperatingSystem.MAC
        isUnix() -> OperatingSystem.UNIX
        isSolaris() -> OperatingSystem.SOLARIS
        else -> throw IllegalStateException("Unknown operating system: $OPERATING_SYSTEM")
    }
}
