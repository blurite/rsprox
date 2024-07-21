package net.rsprox.proxy.config

import net.rsprox.proxy.util.ConnectionInfo
import java.nio.file.Path
import kotlin.io.path.Path

internal val CONFIGURATION_PATH: Path = Path(System.getProperty("user.home"), ".rsprox")
internal val BINARY_PATH: Path = CONFIGURATION_PATH.resolve("binary")
internal val CLIENTS_DIRECTORY: Path = CONFIGURATION_PATH.resolve("clients")
internal val PLUGINS_DIRECTORY: Path = CONFIGURATION_PATH.resolve("plugins")
internal val TRANSCRIBERS_DIRECTORY: Path = CONFIGURATION_PATH.resolve("transcribers")
internal val TEMP_CLIENTS_DIRECTORY: Path = CLIENTS_DIRECTORY.resolve("temp")
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
