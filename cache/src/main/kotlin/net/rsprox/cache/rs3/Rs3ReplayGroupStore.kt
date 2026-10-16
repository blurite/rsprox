package net.rsprox.cache.rs3

import io.netty.buffer.Unpooled
import net.rsprox.cache.CACHES_DIRECTORY
import net.rsprox.cache.dictionary.openrs2.CacheScope
import net.rsprox.cache.util.downloadOpenRs2Group
import org.openrs2.buffer.use
import org.openrs2.cache.Js5Compression
import org.openrs2.cache.Js5Index
import java.nio.ByteBuffer
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.zip.CRC32

/** Exact recorded groups, fetched lazily. No complete-cache download or live-cache fallback. */
public class Rs3ReplayGroupStore(
    masterIndex: ByteArray,
    private val directory: Path = CACHES_DIRECTORY.resolve("rs3-live"),
    private val download: (Int, Int, Int, Int) -> ByteArray = { archive, group, version, checksum ->
        downloadOpenRs2Group(CacheScope.RuneScape, archive, group, version, checksum, 32 * 1024 * 1024 + 9)
    },
) : AutoCloseable {
    private data class Entry(
        val version: Int,
        val checksum: Int,
    )

    private data class Request(
        val archive: Int,
        val group: Int,
    )

    private val master = masterIndex.copyOf()
    private val archives: List<Entry>
    private val indexes = ConcurrentHashMap<Int, Js5Index>()
    private val pending = ConcurrentHashMap<Request, CompletableFuture<ByteArray>>()
    private val closed = AtomicBoolean()
    private val workers =
        ThreadPoolExecutor(
            4,
            4,
            0,
            TimeUnit.MILLISECONDS,
            ArrayBlockingQueue(256),
            { task -> Thread(task, "rs3-replay-cache").apply { isDaemon = true } },
            ThreadPoolExecutor.AbortPolicy(),
        )

    init {
        val buffer = ByteBuffer.wrap(master)
        require(buffer.hasRemaining()) { "Missing recorded RS3 master index" }
        val count = buffer.get().toInt() and 255
        require(count > 0 && buffer.remaining() >= count * 80) { "Truncated recorded RS3 master index" }
        archives =
            List(count) {
                val checksum = buffer.int
                val version = buffer.int
                buffer.position(buffer.position() + 72)
                Entry(version, checksum)
            }
    }

    /** Returns a JS5 container, without the disk-store version trailer. Never blocks the caller. */
    public fun get(
        archive: Int,
        group: Int,
    ): CompletableFuture<ByteArray> {
        if (closed.get()) return failed(IllegalStateException("Replay cache is closed"))
        if (archive !in 0..255 || group < 0) return failed(IllegalArgumentException("Invalid cache request"))
        val request = Request(archive, group)
        val future = CompletableFuture<ByteArray>()
        pending.putIfAbsent(request, future)?.let { return it.thenApply(ByteArray::copyOf) }
        try {
            workers.execute {
                try {
                    val bytes =
                        if (archive == 255 && group == 255) {
                            // The binary retains the complete uncompressed signed master-index body.
                            ByteBuffer
                                .allocate(5 + master.size)
                                .put(0)
                                .putInt(master.size)
                                .put(master)
                                .array()
                        } else {
                            val entry =
                                if (archive == 255) {
                                    archives.getOrNull(group) ?: error("Unknown recorded archive $group")
                                } else {
                                    val index = indexes.computeIfAbsent(archive, ::loadIndex)
                                    val metadata = index[group] ?: error("Unknown recorded group $archive:$group")
                                    Entry(metadata.version, metadata.checksum)
                                }
                            readGroup(archive, group, entry)
                        }
                    future.complete(bytes)
                } catch (error: Exception) {
                    future.completeExceptionally(error)
                } finally {
                    pending.remove(request, future)
                }
            }
        } catch (error: Exception) {
            pending.remove(request, future)
            future.completeExceptionally(error)
        }
        return future.thenApply(ByteArray::copyOf)
    }

    private fun loadIndex(archive: Int): Js5Index {
        val entry = archives.getOrNull(archive) ?: error("Unknown recorded archive $archive")
        // Resolve on this worker, not by waiting on another task from the same bounded pool.
        val bytes = readGroup(255, archive, entry)
        return Unpooled.wrappedBuffer(bytes).use { compressed ->
            Js5Compression.uncompress(compressed).use { buffer ->
                Js5Index.read(buffer).also {
                    require(it.version == entry.version && !buffer.isReadable) { "Invalid recorded index $archive" }
                }
            }
        }
    }

    private fun readGroup(
        archive: Int,
        group: Int,
        entry: Entry,
    ): ByteArray {
        val path = directory.resolve("$archive/${group}_${entry.version}_${entry.checksum.toUInt().toString(16)}.dat")
        if (Files.isRegularFile(path) && Files.size(path) <= MAX_CONTAINER_SIZE) {
            val bytes = Files.readAllBytes(path)
            if (valid(bytes, entry)) return bytes
        }
        val bytes = download(archive, group, entry.version, entry.checksum)
        require(valid(bytes, entry)) { "Invalid recorded RS3 group $archive:$group (version ${entry.version})" }
        Files.createDirectories(path.parent)
        val temporary = Files.createTempFile(path.parent, "replay-", ".tmp")
        try {
            Files.write(temporary, bytes)
            Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING)
        } finally {
            Files.deleteIfExists(temporary)
        }
        return bytes
    }

    private fun valid(
        bytes: ByteArray,
        entry: Entry,
    ): Boolean {
        if (bytes.size !in 5..MAX_CONTAINER_SIZE) return false
        val buffer = ByteBuffer.wrap(bytes)
        val compression = buffer.get().toInt() and 255
        val length = buffer.int
        if (compression !in 0..3 || length < 0) return false
        if (length.toLong() + (if (compression == 0) 5 else 9) != bytes.size.toLong()) return false
        return CRC32().apply { update(bytes) }.value.toInt() == entry.checksum
    }

    override fun close() {
        if (!closed.compareAndSet(false, true)) return
        pending.values.forEach { it.completeExceptionally(IllegalStateException("Replay cache is closed")) }
        workers.shutdownNow()
        pending.clear()
        indexes.clear()
    }

    private fun failed(error: Exception): CompletableFuture<ByteArray> =
        CompletableFuture<ByteArray>().also { it.completeExceptionally(error) }

    private companion object {
        const val MAX_CONTAINER_SIZE = 32 * 1024 * 1024 + 9
    }
}
