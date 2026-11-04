package net.rsprox.cache.store

import com.github.michaelbull.logging.InlineLogger
import net.rsprox.cache.CACHES_DIRECTORY
import net.rsprox.cache.Js5MasterIndex
import net.rsprox.cache.OPENRS2_DISK_CACHES_DIRECTORY
import net.rsprox.cache.RSPROX_ARCHIVE_DISK_CACHES_DIRECTORY
import net.rsprox.cache.util.atomicWrite
import org.openrs2.buffer.use
import java.nio.file.Files
import java.nio.file.Path
import java.util.zip.CRC32

internal class HistoricGroupCache(
    masterIndex: Js5MasterIndex,
    private val local: DiskGroupStore?,
    knownCacheId: Int?,
) : AutoCloseable {
    private val installed = lazy {
        buildList {
            val archived = RSPROX_ARCHIVE_DISK_CACHES_DIRECTORY.resolve(masterIndex.sha256())
            if (Files.isRegularFile(archived.resolve(".rsprox-archive-disk.complete"))) add(archived)
            if (Files.isDirectory(OPENRS2_DISK_CACHES_DIRECTORY)) {
                Files.list(OPENRS2_DISK_CACHES_DIRECTORY).use { dirs ->
                    addAll(
                        dirs.iterator().asSequence()
                            .filter { Files.isRegularFile(it.resolve(".openrs2-disk.complete")) }
                            .sortedWith(
                                compareByDescending<Path> { it.fileName.toString() == knownCacheId?.toString() }
                                    .thenByDescending { it.fileName.toString().toIntOrNull() },
                            )
                            .toList(),
                    )
                }
            }
        }.map { LocalReplayDiskCacheStore(it.resolve("cache")) }
    }

    fun read(archive: Int, group: Int, version: Int, checksum: Int): ByteArray {
        val path = CACHES_DIRECTORY.resolve("openrs2-groups/$archive/${group}_${version}_${checksum.toUInt().toString(16)}.dat")
        return synchronized(locks[path.hashCode().and(Int.MAX_VALUE) % locks.size]) {
            if (Files.isRegularFile(path)) {
                val bytes = Files.readAllBytes(path)
                if (valid(bytes, checksum)) return@synchronized bytes
            }
            // The archive index may already be in the recording's loose cache.
            local?.get(archive, group)?.use { buf ->
                val bytes = ByteArray(buf.readableBytes()).also { buf.readBytes(it) }
                if (valid(bytes, checksum)) return@synchronized bytes
                if (bytes.size >= 2 && valid(bytes, checksum, bytes.size - 2)) {
                    return@synchronized bytes.copyOf(bytes.size - 2)
                }
            }
            for (store in installed.value) {
                val result =
                    try {
                        store.get(archive, group)
                    } catch (e: Exception) {
                        logger.debug(e) { "Unable to read installed cache group $archive:$group" }
                        null
                    }
                result?.use { buf ->
                    val bytes = ByteArray(buf.readableBytes()).also { buf.readBytes(it) }
                    if (valid(bytes, checksum)) return@synchronized bytes
                }
            }
            val bytes = OpenRs2GroupStore.download(archive, group, version, checksum)
            Files.createDirectories(path.parent)
            path.atomicWrite(bytes)
            bytes
        }
    }

    override fun close() {
        if (installed.isInitialized()) installed.value.forEach { it.close() }
    }

    private fun valid(bytes: ByteArray, checksum: Int, length: Int = bytes.size): Boolean =
        CRC32().apply { update(bytes, 0, length) }.value.toInt() == checksum

    private companion object {
        private val logger = InlineLogger()
        private val locks = Array(64) { Any() }
    }
}
