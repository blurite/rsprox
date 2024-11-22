package net.rsprox.proxy.config

import net.rsprox.proxy.util.ConnectionInfo
import java.nio.file.Path
import kotlin.io.path.Path

internal val CONFIGURATION_PATH: Path = Path(System.getProperty("user.home"), ".rsprox")
public val BINARY_PATH: Path = CONFIGURATION_PATH.resolve("binary")
internal val CLIENTS_DIRECTORY: Path = CONFIGURATION_PATH.resolve("clients")
internal val TEMP_CLIENTS_DIRECTORY: Path = CLIENTS_DIRECTORY.resolve("temp")
internal val CACHES_DIRECTORY: Path = CONFIGURATION_PATH.resolve("caches")
internal val FILTERS_DIRECTORY: Path = CONFIGURATION_PATH.resolve("filters")
internal val SETTINGS_DIRECTORY: Path = CONFIGURATION_PATH.resolve("settings")
internal val SOCKETS_DIRECTORY: Path = CONFIGURATION_PATH.resolve("sockets")
internal val SIGN_KEY_DIRECTORY: Path = CONFIGURATION_PATH.resolve("signkey")
internal val BINARY_CREDENTIALS_FOLDER: Path = CONFIGURATION_PATH.resolve("credentials")
internal val BINARY_CREDENTIALS: Path = BINARY_CREDENTIALS_FOLDER.resolve("binary.credentials")
internal val FAKE_CERTIFICATE_FILE: Path = SIGN_KEY_DIRECTORY.resolve("fake-cert.jks")
internal val JAGEX_ACCOUNTS_FILE: Path = CONFIGURATION_PATH.resolve("jagex-accounts.properties")
internal val RUNELITE_LAUNCHER_REPO_DIRECTORY: Path = CONFIGURATION_PATH.resolve("runelite-launcher")
internal const val CURRENT_REVISION: Int = 227
internal const val LATEST_SUPPORTED_PLUGIN: Int = 227

/**
 * Http server port needs to be hard-coded as we modify it in a few RuneLite classes directly.
 * If it wasn't for that, it could be user-configurable.
 */
internal const val HTTP_SERVER_PORT: Int = 43600
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
