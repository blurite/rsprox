package net.rsprox.proxy.rs3.config

import net.rsprox.cache.rs3.Rs3Js5ConnectionInfo
import net.rsprox.patch.native.NativePatchCriteria
import java.net.URL

@JvmInline
public value class Rs3JavConfig(
    public val text: String,
) {
    public constructor(url: URL) : this(url.readText(Charsets.ISO_8859_1))

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
        val lobbyId =
            checkNotNull(getParamValue(LOBBY_ID_PARAM_ID)?.toIntOrNull()) {
                "param=$LOBBY_ID_PARAM_ID (lobby id) not found in jav_config"
            }
        require(lobbyId in 0..65535) { "Lobby id out of bounds: $lobbyId" }
        val lobbyHost =
            checkNotNull(getParamValue(LOBBY_HOST_PARAM_ID)) {
                "param=$LOBBY_HOST_PARAM_ID (lobby host) not found in jav_config"
            }
        val revision = getServerVersion()

        fun port(id: Int): Int {
            val value = getParamValue(id)?.toIntOrNull()
            require(value != null && value in 1..65535) { "Missing or invalid RS3 lobby port parameter $id" }
            return value
        }
        return Rs3UpstreamTargets(
            lobbyId,
            lobbyHost,
            port(LOBBY_PORT_PARAM_ID),
            port(LOBBY_ALTERNATE_PORT_PARAM_ID),
            revision,
        )
    }

    public fun captureJs5ConnectionInfo(): Rs3Js5ConnectionInfo =
        Rs3Js5ConnectionInfo(
            host = checkNotNull(getParamValue(37)) { "RS3 jav_config has no JS5 host" },
            port = checkNotNull(getParamValue(41)?.toIntOrNull()) { "RS3 jav_config has no JS5 port" },
            revision = getServerVersion(),
            token = checkNotNull(getParamValue(29)) { "RS3 jav_config has no JS5 token" },
        )

    public fun rewriteLobbyEndpoint(
        localHost: String,
        primaryPort: Int,
        alternatePort: Int,
    ): Rs3JavConfig {
        require(localHost.isNotBlank() && localHost.none { it.isWhitespace() || it == '\u0000' })
        require(primaryPort in 1..65535 && alternatePort in 1..65535)
        val replacements =
            mapOf(
                "param=$LOBBY_HOST_PARAM_ID=" to localHost,
                "param=$LOBBY_PORT_PARAM_ID=" to primaryPort.toString(),
                "param=$LOBBY_ALTERNATE_PORT_PARAM_ID=" to alternatePort.toString(),
            )
        for (prefix in replacements.keys) {
            require(text.lineSequence().count { it.startsWith(prefix) } == 1) {
                "Expected exactly one RS3 lobby parameter $prefix"
            }
        }
        val rewritten =
            text
                .lineSequence()
                .joinToString("\n") { line ->
                    val replacement = replacements.entries.firstOrNull { line.startsWith(it.key) }
                    if (replacement != null) replacement.key + replacement.value else line
                }
        return Rs3JavConfig(rewritten)
    }

    override fun toString(): String = text

    public companion object {
        public const val DEFAULT_URL: String = NativePatchCriteria.DEFAULT_RS3_JAVCONFIG_URL
        private const val CODEBASE_PREFIX = "codebase="
        private const val SERVER_VERSION_PREFIX = "server_version="
        private const val LOBBY_ID_PARAM_ID = 2
        private const val LOBBY_HOST_PARAM_ID = 3
        private const val LOBBY_PORT_PARAM_ID = 47
        private const val LOBBY_ALTERNATE_PORT_PARAM_ID = 48
    }
}
