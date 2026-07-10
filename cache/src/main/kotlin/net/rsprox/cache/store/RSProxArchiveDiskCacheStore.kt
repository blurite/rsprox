package net.rsprox.cache.store

import com.github.michaelbull.logging.InlineLogger
import io.netty.buffer.ByteBuf
import net.rsprox.cache.util.openRSProxArchiveCache
import org.openrs2.cache.DiskStore
import org.openrs2.cache.Store
import org.openrs2.cache.VersionTrailer
import java.io.BufferedInputStream
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.security.DigestInputStream
import java.security.MessageDigest
import java.util.Comparator
import java.util.concurrent.ConcurrentHashMap
import java.util.zip.ZipInputStream
import kotlin.io.path.exists

public class RSProxArchiveDiskCacheStore internal constructor(
    private val path: Path,
    private val cacheZipUrl: String,
    private val cacheZipSha256: String,
    private val cacheZipSize: Long,
) : ReplayDiskCacheStore {
    private val storePath: Path = path.resolve(CACHE_DIRECTORY)
    private val marker: Path = path.resolve(MARKER_FILE)
    private val lock: Any = locks.computeIfAbsent(path.toAbsolutePath().normalize()) { Any() }
    private var store: Store? = null

    override fun get(
        archive: Int,
        group: Int,
    ): ByteBuf? {
        return synchronized(lock) {
            read(openStore(), archive, group)
        }
    }

    override fun open() {
        synchronized(lock) {
            openStore()
        }
    }

    private fun hydrateIfNeeded() {
        if (isHydrated()) {
            return
        }
        synchronized(lock) {
            if (isHydrated()) {
                return
            }
            Files.createDirectories(path)
            val zipPath = path.resolve(DOWNLOAD_FILE)
            val stagingPath = path.resolve(STAGING_DIRECTORY)
            deleteTree(stagingPath)
            try {
                logger.info {
                    "Downloading RSProx Archive cache into ${path.toAbsolutePath()}"
                }
                val actualSha256 = download(zipPath)
                check(actualSha256.equals(cacheZipSha256, ignoreCase = true)) {
                    "RSProx Archive cache ZIP SHA-256 mismatch: expected $cacheZipSha256, " +
                        "got $actualSha256"
                }
                check(Files.size(zipPath) == cacheZipSize) {
                    "RSProx Archive cache ZIP size mismatch: expected $cacheZipSize, " +
                        "got ${Files.size(zipPath)}"
                }
                extract(zipPath, stagingPath)
                check(Files.isRegularFile(stagingPath.resolve(DATA_FILE), LinkOption.NOFOLLOW_LINKS)) {
                    "RSProx Archive cache ZIP does not contain $DATA_FILE"
                }
                deleteTree(storePath)
                moveDirectory(stagingPath, storePath)
                writeMarker()
                logger.info { "Finished extracting RSProx Archive cache" }
            } finally {
                Files.deleteIfExists(zipPath)
                deleteTree(stagingPath)
            }
        }
    }

    private fun isHydrated(): Boolean =
        marker.exists(LinkOption.NOFOLLOW_LINKS) &&
            Files.isRegularFile(storePath.resolve(DATA_FILE), LinkOption.NOFOLLOW_LINKS)

    override fun close() {
        synchronized(lock) {
            store?.close()
            store = null
        }
    }

    private fun openStore(): Store {
        hydrateIfNeeded()
        val existing = store
        if (existing != null) {
            return existing
        }
        val opened = DiskStore.open(storePath)
        store = opened
        return opened
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun download(destination: Path): String {
        val digest = MessageDigest.getInstance("SHA-256")
        openRSProxArchiveCache(cacheZipUrl).use { input ->
            DigestInputStream(BufferedInputStream(input), digest).use { verified ->
                Files.copy(verified, destination, StandardCopyOption.REPLACE_EXISTING)
            }
        }
        return digest.digest().toHexString()
    }

    private fun extract(
        zipPath: Path,
        destination: Path,
    ) {
        Files.createDirectories(destination)
        var files = 0
        ZipInputStream(BufferedInputStream(Files.newInputStream(zipPath))).use { zip ->
            while (true) {
                val entry = zip.nextEntry ?: break
                try {
                    val target = resolveZipEntry(destination, entry.name)
                    if (entry.isDirectory) {
                        Files.createDirectories(target)
                    } else {
                        Files.createDirectories(target.parent)
                        Files.copy(zip, target, StandardCopyOption.REPLACE_EXISTING)
                        files++
                    }
                } finally {
                    zip.closeEntry()
                }
            }
        }
        check(files > 0) { "RSProx Archive cache ZIP contains no files" }
    }

    private fun resolveZipEntry(
        destination: Path,
        entryName: String,
    ): Path {
        require(entryName.isNotBlank()) { "RSProx Archive cache ZIP contains an empty entry name" }
        val normalizedDestination = destination.toAbsolutePath().normalize()
        val target = normalizedDestination.resolve(entryName).normalize()
        check(target.startsWith(normalizedDestination)) {
            "Refusing to extract RSProx Archive cache entry outside destination: $entryName"
        }
        return target
    }

    private fun read(
        store: Store,
        archive: Int,
        group: Int,
    ): ByteBuf? {
        return try {
            val buffer = store.read(archive, group)
            if (archive != Store.ARCHIVESET) {
                VersionTrailer.strip(buffer)
            }
            buffer
        } catch (error: Exception) {
            logger.debug(error) {
                "Unable to read RSProx Archive cache group $archive:$group"
            }
            null
        }
    }

    private fun writeMarker() {
        val temporary = path.resolve(".$MARKER_FILE")
        Files.writeString(
            temporary,
            "url=$cacheZipUrl\nsha256=$cacheZipSha256\n",
        )
        try {
            Files.move(
                temporary,
                marker,
                StandardCopyOption.REPLACE_EXISTING,
                StandardCopyOption.ATOMIC_MOVE,
            )
        } catch (_: AtomicMoveNotSupportedException) {
            Files.move(temporary, marker, StandardCopyOption.REPLACE_EXISTING)
        } finally {
            Files.deleteIfExists(temporary)
        }
    }

    private fun moveDirectory(
        source: Path,
        destination: Path,
    ) {
        try {
            Files.move(source, destination, StandardCopyOption.ATOMIC_MOVE)
        } catch (_: AtomicMoveNotSupportedException) {
            Files.move(source, destination)
        }
    }

    private companion object {
        private const val MARKER_FILE: String = ".rsprox-archive-disk.complete"
        private const val CACHE_DIRECTORY: String = "cache"
        private const val STAGING_DIRECTORY: String = "cache.part"
        private const val DOWNLOAD_FILE: String = "cache.zip.part"
        private const val DATA_FILE: String = "main_file_cache.dat2"
        private val logger = InlineLogger()
        private val locks: MutableMap<Path, Any> = ConcurrentHashMap()

        private fun deleteTree(root: Path) {
            if (!root.exists(LinkOption.NOFOLLOW_LINKS)) {
                return
            }
            Files.walk(root).use { paths ->
                paths
                    .sorted(Comparator.reverseOrder())
                    .forEach(Files::deleteIfExists)
            }
        }
    }
}
