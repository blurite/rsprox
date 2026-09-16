package net.rsprox.proxy.rs3.config

import net.rsprox.patch.native.NativePatchCriteria
import net.rsprox.cache.rs3.Rs3Js5ConnectionInfo
import java.net.URL

@JvmInline
public value class Rs3JavConfig(
    public val text: String,
) {
    public constructor(url: URL) : this(url.readText(Charsets.UTF_8))

    public fun getCodebase(): String {
        return text
            .lineSequence()
            .first { it.startsWith(CODEBASE_PREFIX) }
            .substring(CODEBASE_PREFIX.length)
    }

    public fun getParamValue(id: Int): String? {
        val prefix = "param=$id="
        return text
            .lineSequence()
            .firstOrNull { it.startsWith(prefix) }
            ?.substring(prefix.length)
    }

    public fun getDownloadName(id: Int): String? {
        val prefix = "download_name_$id="
        return text
            .lineSequence()
            .firstOrNull { it.startsWith(prefix) }
            ?.substring(prefix.length)
    }

    public fun getDownloadCrc(id: Int): Long? {
        val prefix = "download_crc_$id="
        return text
            .lineSequence()
            .firstOrNull { it.startsWith(prefix) }
            ?.substring(prefix.length)
            ?.toLongOrNull()
    }

    public fun getServerVersion(): Int {
        val prefix = SERVER_VERSION_PREFIX
        return text
            .lineSequence()
            .firstOrNull { it.startsWith(prefix) }
            ?.substring(prefix.length)
            ?.trim()
            ?.toIntOrNull()
            ?: error("RS3 jav_config has no server_version")
    }

    public fun overrideDownloadCrc(
        id: Int,
        crc32: Long,
    ): Rs3JavConfig {
        val prefix = "download_crc_$id="
        val rewritten =
            text
                .lineSequence()
                .joinToString("\n") { line ->
                    if (line.startsWith(prefix)) "$prefix$crc32" else line
                }
        return Rs3JavConfig(rewritten)
    }

    public fun clearDownloadHash(id: Int): Rs3JavConfig {
        val prefix = "download_hash_$id="
        val rewritten =
            text
                .lineSequence()
                .joinToString("\n") { line ->
                    if (line.startsWith(prefix)) prefix else line
                }
        return Rs3JavConfig(rewritten)
    }

    public fun overrideDownloadHash(
        id: Int,
        hash: String,
    ): Rs3JavConfig {
        val prefix = "download_hash_$id="
        val rewritten =
            text
                .lineSequence()
                .joinToString("\n") { line ->
                    if (line.startsWith(prefix)) "$prefix$hash" else line
                }
        return Rs3JavConfig(rewritten)
    }

    public fun captureUpstreamTargets(): Rs3UpstreamTargets {
        val lobbyHost =
            checkNotNull(getParamValue(LOBBY_HOST_PARAM_ID)) {
                "param=$LOBBY_HOST_PARAM_ID (lobby host) not found in jav_config"
            }
        val gamePort =
            checkNotNull(getParamValue(GAME_PORT_PARAM_ID)?.toIntOrNull()) {
                "param=$GAME_PORT_PARAM_ID (game port) not found in jav_config"
            }
        val revision = getServerVersion()
        return Rs3UpstreamTargets(lobbyHost, gamePort, revision)
    }

    public fun captureJs5ConnectionInfo(): Rs3Js5ConnectionInfo = Rs3Js5ConnectionInfo(
        host = checkNotNull(getParamValue(37)) { "RS3 jav_config has no JS5 host" },
        port = checkNotNull(getParamValue(41)?.toIntOrNull()) { "RS3 jav_config has no JS5 port" },
        revision = getServerVersion(),
        token = checkNotNull(getParamValue(29)) { "RS3 jav_config has no JS5 token" },
    )

    public fun rewriteLobbyHost(localHost: String): Rs3JavConfig {
        val prefix = "param=$LOBBY_HOST_PARAM_ID="
        val rewritten =
            text
                .lineSequence()
                .joinToString("\n") { line ->
                    if (line.startsWith(prefix)) "$prefix$localHost" else line
                }
        return Rs3JavConfig(rewritten)
    }

    override fun toString(): String = text

    public companion object {
        public const val DEFAULT_URL: String = NativePatchCriteria.DEFAULT_RS3_JAVCONFIG_URL
        private const val CODEBASE_PREFIX = "codebase="
        private const val SERVER_VERSION_PREFIX = "server_version="
        private const val LOBBY_HOST_PARAM_ID = 3
        private const val GAME_PORT_PARAM_ID = 41
    }
}
