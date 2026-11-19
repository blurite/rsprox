package net.rsprox.proxy.downloader

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import net.rsprox.proxy.config.CLIENTS_DIRECTORY
import net.rsprox.proxy.rs3.Rs3LaunchProgress
import net.rsprox.proxy.rs3.Rs3ProtocolRevision
import net.rsprox.proxy.rs3.config.Rs3JavConfig
import java.io.IOException
import java.net.URI
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption.ATOMIC_MOVE
import java.nio.file.StandardCopyOption.REPLACE_EXISTING
import java.security.MessageDigest
import java.time.Instant
import java.util.zip.CRC32

internal object RSProxArchiveRs3ClientDownloader {
    private const val BASE_URL = "https://archive.rsprox.net/"
    private const val INDEX_PATH = "native-clients/runescape/index.json"
    private val beta950WorldChange = Instant.parse("2026-10-19T00:00:00Z")
    private val directory = CLIENTS_DIRECTORY.resolve("rs3-archive")
    private val mapper = jacksonObjectMapper()

    @Synchronized
    fun live(
        config: Rs3JavConfig,
        binaryType: Int,
        beta: Boolean,
        onProgress: (Rs3LaunchProgress) -> Unit,
    ): Path {
        Rs3ProtocolRevision.supported(Rs3ProtocolRevision.of(config.getServerVersion(), if (beta) 1 else 0).value)
        val crc = checkNotNull(config.getDownloadCrc(0)) { "Missing RS3 client CRC" } and 0xffffffffL
        val name = checkNotNull(config.getDownloadName(0)) { "Missing RS3 client filename" }
        val signature =
            config.text
                .lineSequence()
                .firstOrNull { it.startsWith("download_hash_0=") }
                ?.substringAfter('=')
        onProgress(Rs3LaunchProgress("Checking archived client"))
        val entry =
            loadIndex()
                .clients
                .filter {
                    it.revision == config.getServerVersion() &&
                        it.binaryType == binaryType &&
                        it.environment == if (beta) "beta" else "live"
                }.filter { entry ->
                    entry.files.any {
                        it.slot == 0 &&
                            it.name == name &&
                            it.crc32 == crc &&
                            (signature.isNullOrEmpty() || it.advertisedHash == signature)
                    }
                }.maxByOrNull { Instant.parse(it.observedAt) }
                ?: error("The current RS3 client is not archived yet. Please retry after RuneArchive's next sync.")
        return client(entry, onProgress)
    }

    @Synchronized
    fun replay(
        revision: Rs3ProtocolRevision,
        binaryType: Int,
        worldId: Int,
        recordedAt: Instant,
        onProgress: (Rs3LaunchProgress) -> Unit,
    ): ArchivedReplayClient {
        Rs3ProtocolRevision.supported(revision.value)
        onProgress(Rs3LaunchProgress("Finding archived replay client"))
        val environment = if (revision.obfuscationVersion == 0) "live" else "beta"
        val candidates =
            loadIndex()
                .clients
                .filter {
                    it.revision == revision.wireRevision &&
                        it.binaryType == binaryType &&
                        it.environment == environment &&
                        it.files.any { file -> file.slot == 0 && file.name == "rs2client.exe" }
                }.map { Instant.parse(it.observedAt) to it }
                .filter { (observedAt, _) ->
                    // On October 19 the 950 beta switched from world 204 to worlds 203/205.
                    when {
                        revision.value != Rs3ProtocolRevision.BETA_950_1 -> true
                        worldId == 204 -> observedAt < beta950WorldChange
                        worldId == 203 || worldId == 205 -> observedAt >= beta950WorldChange
                        else -> true
                    }
                }.sortedBy { it.first }
        // Recordings can predate RuneArchive's first observation of a client.
        val entry =
            (candidates.lastOrNull { it.first <= recordedAt } ?: candidates.firstOrNull())?.second
                ?: error("No archived RS3 $environment client for revision ${revision.wireRevision}, binary type $binaryType, world $worldId")
        val configuration =
            Rs3JavConfig(Files.readString(artifact(entry.configuration, onProgress), Charsets.ISO_8859_1))
        val executable = entry.files.single { it.slot == 0 }
        check(
            configuration.getServerVersion() == revision.wireRevision &&
                configuration.getDownloadName(0) == executable.name &&
                configuration.getDownloadCrc(0)?.and(0xffffffffL) == executable.crc32,
        ) {
            "Archived RS3 configuration does not match its client"
        }
        return ArchivedReplayClient(client(entry, onProgress), configuration)
    }

    private fun client(
        entry: Client,
        onProgress: (Rs3LaunchProgress) -> Unit,
    ): Path {
        require(entry.binaryType == 2 || entry.binaryType == 10) { "Unsupported RS3 binary type" }
        val file = entry.files.single { it.slot == 0 }
        check(file.name == "rs2client.exe") { "Unexpected RS3 executable" }
        val path = artifact(file.file, onProgress)
        val crc = CRC32()
        Files.newInputStream(path).use { input ->
            val bytes = ByteArray(64 * 1024)
            while (true) {
                val count = input.read(bytes)
                if (count == -1) break
                crc.update(bytes, 0, count)
            }
        }
        check(crc.value == file.crc32) { "Archived RS3 client CRC mismatch" }
        return path
    }

    private fun loadIndex(): Index {
        Files.createDirectories(directory)
        val cached = directory.resolve("index.json")
        val bytes =
            try {
                connection(INDEX_PATH).getInputStream().use { input ->
                    input.readNBytes(16 * 1024 * 1024 + 1).also {
                        check(it.size <= 16 * 1024 * 1024) { "RS3 archive catalog exceeds size limit" }
                    }
                }
            } catch (error: IOException) {
                // Previously verified immutable objects remain usable while the archive is unavailable.
                if (!Files.isRegularFile(cached)) throw error
                Files.readAllBytes(cached)
            }
        val index: Index = mapper.readValue(bytes)
        check(index.version == 1 && index.kind == "rs3-client-index" && index.game == "runescape") {
            "Unsupported RS3 client archive catalog"
        }
        val temporary = Files.createTempFile(directory, "index-", ".tmp")
        try {
            Files.write(temporary, bytes)
            move(temporary, cached)
        } finally {
            Files.deleteIfExists(temporary)
        }
        return index
    }

    private fun artifact(
        reference: Artifact,
        onProgress: (Rs3LaunchProgress) -> Unit,
    ): Path {
        require(reference.sha256.matches(Regex("[0-9a-f]{64}")) && reference.size in 1..512L * 1024 * 1024)
        val name = reference.path.substringAfterLast('/')
        require(name.matches(Regex("[A-Za-z0-9_.-]{1,128}")) && name != "." && name != "..")
        require(reference.path == "native-clients/runescape/objects/${reference.sha256}/$name") {
            "Invalid RS3 archive object path"
        }
        val target = directory.resolve(reference.sha256).resolve(name)
        if (Files.isRegularFile(target) && Files.size(target) == reference.size && sha256(target) == reference.sha256) {
            return target
        }
        Files.createDirectories(target.parent)
        val temporary = Files.createTempFile(target.parent, "download-", ".tmp")
        try {
            val download = connection(reference.path)
            download.getInputStream().use { input ->
                Files.newOutputStream(temporary).use { output ->
                    val buffer = ByteArray(64 * 1024)
                    var received = 0L
                    while (true) {
                        val count = input.read(buffer)
                        if (count == -1) break
                        received += count
                        check(received <= reference.size) { "Archived RS3 file exceeds expected size" }
                        output.write(buffer, 0, count)
                        onProgress(Rs3LaunchProgress("Downloading archived client", received, reference.size))
                    }
                    check(received == reference.size) { "Truncated archived RS3 file" }
                }
            }
            check(sha256(temporary) == reference.sha256) { "Archived RS3 file SHA-256 mismatch" }
            move(temporary, target)
        } finally {
            Files.deleteIfExists(temporary)
        }
        return target
    }

    private fun connection(path: String) =
        URI(BASE_URL + path).toURL().openConnection().apply {
            connectTimeout = 10_000
            readTimeout = 30_000
        }

    private fun sha256(path: Path): String {
        val digest = MessageDigest.getInstance("SHA-256")
        Files.newInputStream(path).use { input ->
            val bytes = ByteArray(64 * 1024)
            while (true) {
                val count = input.read(bytes)
                if (count == -1) break
                digest.update(bytes, 0, count)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it.toInt() and 255) }
    }

    private fun move(
        source: Path,
        target: Path,
    ) {
        try {
            Files.move(source, target, ATOMIC_MOVE, REPLACE_EXISTING)
        } catch (_: AtomicMoveNotSupportedException) {
            Files.move(source, target, REPLACE_EXISTING)
        }
    }

    internal data class ArchivedReplayClient(
        val executable: Path,
        val configuration: Rs3JavConfig,
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    private data class Index(
        val version: Int,
        val kind: String,
        val game: String,
        val clients: List<Client>,
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    private data class Client(
        val environment: String,
        val binaryType: Int,
        val revision: Int,
        val observedAt: String,
        val configuration: Artifact,
        val files: List<ClientFile>,
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    private data class ClientFile(
        val slot: Int,
        val name: String,
        val crc32: Long,
        val advertisedHash: String?,
        val file: Artifact,
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    private data class Artifact(
        val path: String,
        val sha256: String,
        val size: Long,
    )
}
