package net.rsprox.cache.store

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import net.rsprox.cache.Js5MasterIndex
import net.rsprox.cache.dictionary.openrs2.CacheScope
import net.rsprox.cache.util.downloadOpenRs2Group
import org.openrs2.buffer.use
import org.openrs2.cache.Js5Compression
import org.openrs2.cache.Js5Index
import org.openrs2.cache.MasterIndexFormat
import java.util.concurrent.ConcurrentHashMap
import java.util.zip.CRC32
import org.openrs2.cache.Js5MasterIndex as OpenRs2Js5MasterIndex

public class OpenRs2GroupStore internal constructor(
    private val masterIndex: Js5MasterIndex,
    private val readGroup: (Int, Int, Int, Int) -> ByteArray,
    private val includeVersionTrailer: Boolean = false,
) : GroupStore {
    @Suppress("UNUSED_PARAMETER")
    public constructor(id: Int, masterIndex: Js5MasterIndex) : this(masterIndex, ::download, true)

    private val openRs2MasterIndex: OpenRs2Js5MasterIndex by lazy {
        Unpooled.wrappedBuffer(masterIndex.data).use { compressed ->
            Js5Compression.uncompress(compressed).use {
                OpenRs2Js5MasterIndex.readUnverified(it, MasterIndexFormat.VERSIONED)
            }
        }
    }
    private val indexes = ConcurrentHashMap<Int, Js5Index>()

    override fun get(
        archive: Int,
        group: Int,
    ): ByteBuf {
        if (archive == MASTER_INDEX && group == MASTER_INDEX) {
            return Unpooled.wrappedBuffer(masterIndex.data)
        }
        val (version, checksum) = metadata(archive, group)
        val data = readGroup(archive, group, version, checksum)
        return if (includeVersionTrailer) {
            Unpooled.wrappedBuffer(data, byteArrayOf((version ushr 8).toByte(), version.toByte()))
        } else {
            Unpooled.wrappedBuffer(data)
        }
    }

    private fun metadata(
        archive: Int,
        group: Int,
    ): Pair<Int, Int> {
        return if (archive == MASTER_INDEX) {
            val entry = openRs2MasterIndex.entries[group]
            entry.version to entry.checksum
        } else {
            val index = indexes.computeIfAbsent(archive) { loadIndex(it) }
            val entry =
                checkNotNull(index[group]) {
                    "OpenRS2 group metadata unavailable for $archive:$group"
                }
            entry.version to entry.checksum
        }
    }

    private fun loadIndex(archive: Int): Js5Index {
        check((openRs2MasterIndex.entries.getOrNull(archive)?.checksum ?: 0) != 0) { "Unknown archive: $archive" }
        return get(MASTER_INDEX, archive).use { compressed ->
            Js5Compression.uncompress(compressed).use { uncompressed ->
                Js5Index.read(uncompressed)
            }
        }
    }

    internal companion object {
        private const val MASTER_INDEX: Int = 0xFF

        internal fun download(archive: Int, group: Int, version: Int, checksum: Int): ByteArray {
            val data = downloadOpenRs2Group(CacheScope.RuneScape, archive, group, version, checksum)
            check(CRC32().apply { update(data) }.value.toInt() == checksum) { "OpenRS2 group CRC mismatch for $archive:$group" }
            return data
        }
    }
}
