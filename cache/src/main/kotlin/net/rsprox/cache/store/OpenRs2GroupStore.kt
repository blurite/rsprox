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
import org.openrs2.cache.Js5MasterIndex as OpenRs2Js5MasterIndex
import java.util.zip.CRC32

public class OpenRs2GroupStore(
    private val id: Int,
    private val masterIndex: Js5MasterIndex,
) : GroupStore {
    private val openRs2MasterIndex: OpenRs2Js5MasterIndex =
        Js5Compression.uncompress(Unpooled.wrappedBuffer(masterIndex.data)).use {
            OpenRs2Js5MasterIndex.readUnverified(it, MasterIndexFormat.VERSIONED)
        }
    private val indexes: MutableMap<Int, Js5Index?> = mutableMapOf()

    override fun get(
        archive: Int,
        group: Int,
    ): ByteBuf {
        if (archive == MASTER_INDEX && group == MASTER_INDEX) {
            return Unpooled.wrappedBuffer(masterIndex.data)
        }
        val (version, checksum) = metadata(archive, group)
        val data =
            downloadOpenRs2Group(
                CacheScope.RuneScape,
                archive,
                group,
                version,
                checksum,
            )
        val crc = CRC32()
        crc.update(data)
        check(crc.value.toInt() == checksum) {
            "OpenRS2 group CRC mismatch for $archive:$group, expected $checksum, got ${crc.value.toInt()}"
        }
        return Unpooled.wrappedBuffer(data, byteArrayOf((version ushr 8).toByte(), version.toByte()))
    }

    private fun metadata(
        archive: Int,
        group: Int,
    ): Pair<Int, Int> {
        return if (archive == MASTER_INDEX) {
            val entry = openRs2MasterIndex.entries[group]
            entry.version to entry.checksum
        } else {
            val index = indexes.getOrPut(archive) { loadIndex(archive) }
            val entry = checkNotNull(index?.get(group)) {
                "OpenRS2 group metadata unavailable for $archive:$group"
            }
            entry.version to entry.checksum
        }
    }

    private fun loadIndex(archive: Int): Js5Index? {
        val entry = openRs2MasterIndex.entries.getOrNull(archive) ?: return null
        if (entry.checksum == 0) {
            return null
        }
        return get(MASTER_INDEX, archive).use { compressed ->
            Js5Compression.uncompress(compressed).use { uncompressed ->
                Js5Index.read(uncompressed)
            }
        }
    }

    private companion object {
        private const val MASTER_INDEX: Int = 0xFF
    }
}
