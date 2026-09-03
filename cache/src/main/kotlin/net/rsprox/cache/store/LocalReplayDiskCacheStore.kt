package net.rsprox.cache.store

import com.github.michaelbull.logging.InlineLogger
import io.netty.buffer.ByteBuf
import net.rsprox.cache.Js5MasterIndex
import org.openrs2.cache.DiskStore
import org.openrs2.cache.Store
import org.openrs2.cache.VersionTrailer
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.Path

public enum class ReplayCacheMatch {
    MATCH,
    MISMATCH,
    UNVERIFIABLE,
}

public class LocalReplayDiskCacheStore(
    private val path: Path,
) : ReplayDiskCacheStore {
    private val lock: Any = Any()
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

    public fun match(masterIndex: Js5MasterIndex): ReplayCacheMatch {
        val buffer = get(Store.ARCHIVESET, Store.ARCHIVESET) ?: return ReplayCacheMatch.UNVERIFIABLE
        try {
            val actual = ByteArray(buffer.readableBytes())
            buffer.getBytes(buffer.readerIndex(), actual)
            return if (masterIndex.data contentEquals actual) {
                ReplayCacheMatch.MATCH
            } else {
                ReplayCacheMatch.MISMATCH
            }
        } finally {
            buffer.release()
        }
    }

    override fun close() {
        synchronized(lock) {
            store?.close()
            store = null
        }
    }

    private fun openStore(): Store {
        val existing = store
        if (existing != null) {
            return existing
        }
        require(Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) {
            "Path does not point to a cache directory: $path"
        }
        require(Files.isRegularFile(path.resolve(DATA_FILE), LinkOption.NOFOLLOW_LINKS)) {
            "Cache directory does not contain $DATA_FILE: $path"
        }
        val opened = DiskStore.open(path)
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
        } catch (error: Exception) {
            logger.debug(error) { "Unable to read local replay cache group $archive:$group from $path" }
            null
        }
    }

    private companion object {
        private const val DATA_FILE: String = "main_file_cache.dat2"
        private val logger = InlineLogger()
    }
}
