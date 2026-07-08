package net.rsprox.cache.store

import com.github.michaelbull.logging.InlineLogger
import io.netty.buffer.ByteBuf
import net.rsprox.cache.Js5MasterIndex
import net.rsprox.cache.dictionary.openrs2.CacheScope
import net.rsprox.cache.util.atomicWrite
import net.rsprox.cache.util.openOpenRs2DiskCache
import org.openrs2.cache.DiskStore
import org.openrs2.cache.Store
import org.openrs2.cache.VersionTrailer
import java.io.BufferedInputStream
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.util.concurrent.ConcurrentHashMap
import java.util.zip.ZipInputStream
import kotlin.io.path.exists

public class OpenRs2DiskCacheStore(
    private val cacheId: Int,
    private val path: Path,
    @Suppress("UNUSED_PARAMETER")
    masterIndex: Js5MasterIndex,
) : GroupStore, AutoCloseable {
    private val storePath: Path = path.resolve(ZIP_CACHE_DIRECTORY)
    private val marker: Path = path.resolve(MARKER_FILE)
    private val lock: Any = locks.computeIfAbsent(path.toAbsolutePath().normalize()) { Any() }
    private var store: Store? = null

    override fun get(
        archive: Int,
        group: Int,
    ): ByteBuf? {
        return synchronized(lock) {
            val store = openStore()
            read(store, archive, group)
        }
    }

    public fun getBulk(requests: List<Pair<Int, Int>>): Map<Pair<Int, Int>, ByteBuf> {
        return synchronized(lock) {
            val store = openStore()
            buildMap {
                for (request in requests) {
                    val (archive, group) = request
                    val buffer = read(store, archive, group) ?: continue
                    put(request, buffer)
                }
            }
        }
    }

    public fun open() {
        synchronized(lock) {
            openStore()
        }
    }

    public fun hydrateIfNeeded() {
        if (marker.exists(LinkOption.NOFOLLOW_LINKS)) {
            return
        }
        synchronized(lock) {
            if (marker.exists(LinkOption.NOFOLLOW_LINKS)) {
                return
            }
            Files.createDirectories(path)
            logger.info { "Downloading OpenRS2 disk cache $cacheId into ${path.toAbsolutePath()}" }
            openOpenRs2DiskCache(CacheScope.RuneScape, cacheId).use { response ->
                ZipInputStream(BufferedInputStream(response)).use { zip ->
                    while (true) {
                        val entry = zip.nextEntry ?: break
                        try {
                            if (!entry.isDirectory) {
                                val target = resolveZipEntry(path, entry.name)
                                if (target != null) {
                                    Files.createDirectories(target.parent)
                                    Files.copy(zip, target, StandardCopyOption.REPLACE_EXISTING)
                                }
                            }
                        } finally {
                            zip.closeEntry()
                        }
                    }
                }
            }
            marker.atomicWrite("cacheId=$cacheId\n")
            logger.info { "Finished extracting OpenRS2 disk cache $cacheId" }
        }
    }

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
        } catch (e: Exception) {
            logger.debug(e) { "Unable to read OpenRS2 disk cache group $archive:$group from $cacheId" }
            null
        }
    }

    private fun resolveZipEntry(
        destination: Path,
        entryName: String,
    ): Path? {
        if (!entryName.startsWith("$ZIP_CACHE_DIRECTORY/")) {
            return null
        }
        val normalizedDestination = destination.toAbsolutePath().normalize()
        val target = normalizedDestination.resolve(entryName).normalize()
        check(target.startsWith(normalizedDestination)) {
            "Refusing to extract OpenRS2 disk cache entry outside destination: $entryName"
        }
        return target
    }

    private companion object {
        private const val MARKER_FILE = ".openrs2-disk.complete"
        private const val ZIP_CACHE_DIRECTORY = "cache"
        private val logger = InlineLogger()
        private val locks: MutableMap<Path, Any> = ConcurrentHashMap()
    }
}
