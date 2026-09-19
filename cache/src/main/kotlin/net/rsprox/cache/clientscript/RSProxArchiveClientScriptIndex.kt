package net.rsprox.cache.clientscript

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.michaelbull.logging.InlineLogger
import net.rsprox.cache.CLIENTSCRIPT_INDEXES_DIRECTORY
import net.rsprox.cache.Js5MasterIndex
import net.rsprox.cache.api.type.ClientScriptArgument
import net.rsprox.cache.api.type.ClientScriptDefinition
import net.rsprox.cache.api.type.ClientScriptDefinitionProvider
import net.rsprox.cache.util.atomicWrite
import net.rsprox.cache.util.mapper
import java.net.URI
import java.nio.ByteBuffer
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.isRegularFile

public class RSProxArchiveClientScriptIndex(
    private val masterIndex: Js5MasterIndex,
    private val game: ClientScriptGame = ClientScriptGame.OLD_SCHOOL,
) : ClientScriptDefinitionProvider {
    private val scripts: Map<Int, ClientScriptDefinition> by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        load()
    }

    override fun getClientScriptDefinition(id: Int): ClientScriptDefinition? = scripts[id]

    /** Resolve on the launch/transcription worker, not the relay event loop or Swing thread. */
    public fun preload() {
        scripts
    }

    private fun load(): Map<Int, ClientScriptDefinition> {
        val sha256 = masterIndex.sha256()
        val path = CLIENTSCRIPT_INDEXES_DIRECTORY.resolve(game.namespace).resolve(sha256).resolve(INDEX_FILE)
        if (path.isRegularFile()) {
            try {
                return parse(Files.readAllBytes(path), sha256).also { scripts ->
                    logger.debug { "Loaded ${scripts.size} clientscript definitions from $path" }
                }
            } catch (error: Exception) {
                logger.warn(error) { "Ignoring invalid cached clientscript index at $path" }
                Files.deleteIfExists(path)
            }
        }
        return try {
            download(path, sha256)
        } catch (error: Exception) {
            logger.warn(error) {
                "Unable to load RSProx Archive clientscript index for revision " +
                    "${masterIndex.revision} and master index $sha256"
            }
            emptyMap()
        }
    }

    private fun download(
        path: Path,
        sha256: String,
    ): Map<Int, ClientScriptDefinition> {
        val prefix = listOf("clientscripts", game.namespace).filter(String::isNotEmpty).joinToString("/")
        val uri = URI("$ARCHIVE_BASE_URL/$prefix/$sha256/$INDEX_FILE")
        logger.info { "Downloading clientscript definitions from $uri" }
        val connection = uri.toURL().openConnection()
        connection.connectTimeout = CONNECT_TIMEOUT_MS
        connection.readTimeout = READ_TIMEOUT_MS
        val bytes = connection.getInputStream().use { input -> input.readBytes() }
        val scripts = parse(bytes, sha256)
        Files.createDirectories(path.parent)
        path.atomicWrite(bytes)
        logger.info { "Cached ${scripts.size} clientscript definitions at $path" }
        return scripts
    }

    private fun parse(
        bytes: ByteArray,
        sha256: String,
    ): Map<Int, ClientScriptDefinition> {
        val index = mapper.readValue<ArchivedClientScriptIndex>(bytes)
        require(index.version == INDEX_VERSION && index.kind == INDEX_KIND) {
            "Unsupported clientscript index schema ${index.kind} version ${index.version}"
        }
        require(index.game == game.archiveGame) { "Unexpected clientscript index game: ${index.game}" }
        // Don't allow this check as it breaks during revision transitions.
        // require(index.revision == masterIndex.revision) {
        //     "Clientscript index revision ${index.revision} does not match cache revision ${masterIndex.revision}"
        // }
        require(index.masterIndexSha256.equals(sha256, ignoreCase = true)) {
            "Clientscript index master SHA ${index.masterIndexSha256} does not match $sha256"
        }
        require(index.scriptCount == index.scripts.size) {
            "Clientscript index scriptCount=${index.scriptCount} but scripts contains ${index.scripts.size} entries"
        }
        return index.scripts.mapKeys { (id, _) -> id.toInt() }.mapValues { (_, definition) ->
            ClientScriptDefinition(
                type = definition.type,
                name = definition.name,
                arguments =
                    definition.arguments.map { argument ->
                        ClientScriptArgument(argument.type, argument.name)
                    },
                returnTypes = definition.returnTypes,
            )
        }
    }

    public companion object {
        /** RS3 live snapshots and recording headers contain the uncompressed master index. */
        public fun forRuneScape(revision: Int, masterIndex: ByteArray): RSProxArchiveClientScriptIndex {
            require(masterIndex.isNotEmpty()) { "Missing RS3 master index for clientscript metadata" }
            // OpenRS2/RSProx Archive identify the complete, uncompressed JS5 container (type + length + data).
            val container =
                ByteBuffer.allocate(5 + masterIndex.size)
                    .put(0.toByte())
                    .putInt(masterIndex.size)
                    .put(masterIndex)
                    .array()
            return RSProxArchiveClientScriptIndex(Js5MasterIndex(revision, container), ClientScriptGame.RUNESCAPE)
        }

        private const val ARCHIVE_BASE_URL: String = "https://archive.rsprox.net"
        private const val INDEX_FILE: String = "index.json"
        private const val INDEX_VERSION: Int = 1
        private const val INDEX_KIND: String = "clientscript-index"
        private const val CONNECT_TIMEOUT_MS: Int = 8_000
        private const val READ_TIMEOUT_MS: Int = 30_000
        private val logger = InlineLogger()
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
private data class ArchivedClientScriptIndex(
    val version: Int,
    val kind: String,
    val game: String,
    val revision: Int,
    val masterIndexSha256: String,
    val scriptCount: Int,
    val scripts: Map<String, ArchivedClientScriptDefinition>,
)

@JsonIgnoreProperties(ignoreUnknown = true)
private data class ArchivedClientScriptDefinition(
    val type: String,
    val name: String,
    val arguments: List<ArchivedClientScriptArgument>,
    val returnTypes: List<String>,
)

@JsonIgnoreProperties(ignoreUnknown = true)
private data class ArchivedClientScriptArgument(
    val type: String,
    val name: String,
)
