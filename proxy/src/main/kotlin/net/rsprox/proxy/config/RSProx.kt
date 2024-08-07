package net.rsprox.proxy.config

import net.rsprox.proxy.util.ConnectionInfo
import java.nio.file.Path
import kotlin.io.path.Path

internal val CONFIGURATION_PATH: Path = Path(System.getProperty("user.home"), ".rsprox")
public val BINARY_PATH: Path = CONFIGURATION_PATH.resolve("binary")
internal val CLIENTS_DIRECTORY: Path = CONFIGURATION_PATH.resolve("clients")
internal val PLUGINS_DIRECTORY: Path = CONFIGURATION_PATH.resolve("plugins")
internal val TRANSCRIBERS_DIRECTORY: Path = CONFIGURATION_PATH.resolve("transcribers")
internal val TEMP_CLIENTS_DIRECTORY: Path = CLIENTS_DIRECTORY.resolve("temp")
internal val CACHES_DIRECTORY: Path = CONFIGURATION_PATH.resolve("caches")
internal val FILTERS_DIRECTORY: Path = CONFIGURATION_PATH.resolve("filters")
internal val SOCKETS_DIRECTORY: Path = CONFIGURATION_PATH.resolve("sockets")
internal val RUNELITE_LAUNCHER: Path =
    CONFIGURATION_PATH
        .resolve("runelite")
        .resolve("runelite-launcher.jar")
internal const val RUNELITE_SYSTEM_SPEED: Int = 16384 + 16 + 1024
internal const val CURRENT_REVISION: Int = 223
private val connections: MutableList<ConnectionInfo> = mutableListOf()

internal fun registerConnection(info: ConnectionInfo) {
    check(connections.none { it.port == info.port })
    connections += info
}

public fun getConnection(port: Int): ConnectionInfo {
    return connections.first {
        it.port == port
    }
}
