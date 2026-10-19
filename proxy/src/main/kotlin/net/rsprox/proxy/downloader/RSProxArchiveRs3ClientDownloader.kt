package net.rsprox.proxy.downloader

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import net.rsprox.proxy.config.CLIENTS_DIRECTORY
import net.rsprox.proxy.rs3.Rs3BetaClientBuild
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
        onProgress: (Rs3LaunchProgress) -> Unit,
    ): ArchivedReplayClient {
        onProgress(Rs3LaunchProgress("Finding archived replay client"))
        val entry =
            loadIndex()
                .clients
                .filter {
                    it.revision == revision.wireRevision &&
                        it.binaryType == binaryType &&
                        it.environment == if (revision.obfuscationVersion == 0) "live" else "beta"
                }.filter { entry ->
                    entry.files.any { it.slot == 0 && supportsReplay(revision, binaryType, it.file.sha256) }
                }.maxByOrNull { Instant.parse(it.observedAt) }
                ?: error("No verified archived RS3 client for revision ${revision.value}, binary type $binaryType")
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

    // Note(revision): These live builds are verified with their revision's protocol/patches. A matching
    // revision alone does not prove compatibility. Beta obfuscation identities share their capture guard.
    internal fun supportsReplay(
        revision: Rs3ProtocolRevision,
        binaryType: Int,
        hash: String,
    ): Boolean =
        when (revision.value) {
            Rs3ProtocolRevision.LIVE_950 ->
                when (binaryType) {
                    2 -> hash == "fc7492548627a4068e88a7a45ba4f0be93004adf246bb4b174b24ae588768b36"
                    10 -> hash == "f0dc2a07a8850daa110ea1a7417a540d897551f966f52e64baec1e74a8e2d568"
                    else -> false
                }
            Rs3ProtocolRevision.BETA_950_1 -> Rs3BetaClientBuild.supports(hash, binaryType)
            Rs3ProtocolRevision.LIVE_951 ->
                when (binaryType) {
                    2 -> hash == "f01543b9f7af2a8f4f978abdb2587864ecedb039ab8c406759249be1883d957a"
                    10 -> hash == "e8f49ff0c394b76c939a3b1b1edfcb7bad5139dfee59287a0a28cabdae673538"
                    else -> false
                }
            else -> false
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
