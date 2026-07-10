package net.rsprox.proxy.runelite

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.net.URI
import java.time.Instant

internal object RSProxArchiveBootstrapDictionary {
    fun find(
        timestamp: Long,
        gameRevision: Int,
    ): RSProxArchiveBootstrap? {
        val target = Instant.ofEpochMilli(timestamp)
        return loadIndex()
            .bootstraps
            .asSequence()
            .filter { bootstrap ->
                bootstrap.gameRevision == gameRevision
            }.mapNotNull { bootstrap ->
                val source = bootstrap.source
                val commitSha = source.commitSha ?: return@mapNotNull null
                val committedAt = source.committedAt?.let(Instant::parse) ?: return@mapNotNull null
                RSProxArchiveBootstrap(bootstrap.runeliteVersion, bootstrap.gameRevision, commitSha, committedAt)
            }.filter { bootstrap ->
                !bootstrap.committedAt.isAfter(target)
            }.maxWithOrNull(
                compareBy(RSProxArchiveBootstrap::committedAt, RSProxArchiveBootstrap::commitSha),
            )
    }

    private fun loadIndex(): RSProxArchiveBootstrapIndex {
        val connection = RSPROX_ARCHIVE_RUNELITE_INDEX_URI.toURL().openConnection()
        connection.connectTimeout = CONNECT_TIMEOUT_MS
        connection.readTimeout = READ_TIMEOUT_MS
        val index: RSProxArchiveBootstrapIndex = connection.getInputStream().use(mapper::readValue)
        require(index.kind == RUNELITE_BOOTSTRAP_INDEX_KIND) {
            "Unexpected RSProx Archive index kind: ${index.kind}"
        }
        return index
    }

    private val mapper = jacksonObjectMapper()
    private val RSPROX_ARCHIVE_RUNELITE_INDEX_URI: URI =
        URI("https://archive.rsprox.net/runelite/index.json")

    private const val RUNELITE_BOOTSTRAP_INDEX_KIND: String = "runelite-bootstrap-index"
    private const val CONNECT_TIMEOUT_MS: Int = 5_000
    private const val READ_TIMEOUT_MS: Int = 8_000
}

internal data class RSProxArchiveBootstrap(
    val runeliteVersion: String,
    val gameRevision: Int,
    val commitSha: String,
    val committedAt: Instant,
)

@JsonIgnoreProperties(ignoreUnknown = true)
private data class RSProxArchiveBootstrapIndex(
    val kind: String,
    val bootstraps: List<RSProxArchiveBootstrapEntry>,
)

@JsonIgnoreProperties(ignoreUnknown = true)
private data class RSProxArchiveBootstrapEntry(
    val runeliteVersion: String,
    val gameRevision: Int,
    val source: RSProxArchiveBootstrapSource,
)

@JsonIgnoreProperties(ignoreUnknown = true)
private data class RSProxArchiveBootstrapSource(
    val commitSha: String? = null,
    val committedAt: String? = null,
)
