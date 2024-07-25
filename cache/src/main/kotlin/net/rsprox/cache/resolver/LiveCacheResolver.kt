package net.rsprox.cache.resolver

import com.github.michaelbull.logging.InlineLogger
import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufAllocator
import net.rsprox.cache.CACHES_DIRECTORY
import net.rsprox.cache.DISK_CACHE_DICTIONARY_PATH
import net.rsprox.cache.Js5MasterIndex
import net.rsprox.cache.dictionary.DiskCacheDictionary
import net.rsprox.cache.dictionary.OpenRs2CacheDictionary
import net.rsprox.cache.identifier.DiskCacheIdentifier
import net.rsprox.cache.identifier.OpenRs2CacheIdentifier
import net.rsprox.cache.live.Js5BootstrapFactory
import net.rsprox.cache.live.Js5GroupDownloader
import net.rsprox.cache.live.LiveConnectionInfo
import net.rsprox.cache.store.DiskGroupStore
import net.rsprox.cache.store.OpenRs2GroupStore

public class LiveCacheResolver(
    private val connectionInfo: LiveConnectionInfo,
) : CacheResolver {
    private val diskCacheDictionary: DiskCacheDictionary =
        DiskCacheDictionary.create(
            DISK_CACHE_DICTIONARY_PATH,
            CACHES_DIRECTORY,
        )
    private val openrs2CacheDictionary: OpenRs2CacheDictionary = OpenRs2CacheDictionary()
    private val diskCacheIdentifier: DiskCacheIdentifier = DiskCacheIdentifier(diskCacheDictionary)
    private val openrs2CacheIdentifier: OpenRs2CacheIdentifier =
        OpenRs2CacheIdentifier(
            diskCacheDictionary,
            openrs2CacheDictionary,
        )
    private val js5BootstrapFactory = Js5BootstrapFactory(ByteBufAllocator.DEFAULT)
    private var downloader = Js5GroupDownloader(js5BootstrapFactory, connectionInfo)

    override fun get(
        masterIndex: Js5MasterIndex,
        archive: Int,
        group: Int,
    ): ByteBuf? {
        val local = getLocal(masterIndex, archive, group)
        if (local != null) return local
        val live = getLive(masterIndex, archive, group)
        if (live != null) return live
        val openrs2 = getOpenRs2(masterIndex, archive, group)
        if (openrs2 != null) return openrs2
        logger.warn { "Unable to obtain $archive:$group for ${masterIndex.shortHash()}" }
        return null
    }

    private fun getLocal(
        masterIndex: Js5MasterIndex,
        archive: Int,
        group: Int,
    ): ByteBuf? {
        logger.debug {
            "Attempting to resolve $archive:$group for revision " +
                "${masterIndex.revision} (short-hash: ${masterIndex.shortHash()})"
        }
        logger.debug { "Searching for local variant of $archive:$group" }
        val local =
            resolveLocal(
                masterIndex,
                archive,
                group,
            )
        if (local != null) {
            logger.debug { "Local variant found for $archive:$group" }
            return local
        }
        logger.debug { "Failed to find a local variant of $archive:$group" }
        return null
    }

    private fun getLive(
        masterIndex: Js5MasterIndex,
        archive: Int,
        group: Int,
    ): ByteBuf? {
        if (!this.downloader.isUpToDate(masterIndex)) {
            logger.debug {
                "Downloader master index out of date, establishing a new connection"
            }
            this.downloader.close()
            this.downloader =
                Js5GroupDownloader(
                    this.js5BootstrapFactory,
                    this.connectionInfo.copy(masterIndex = masterIndex),
                )
        }
        val result =
            try {
                this.downloader.get(archive, group)
            } catch (e: Exception) {
                logger.debug { "Unable to retrieve $archive:$group from live servers." }
                return null
            }
        val directory = diskCacheDictionary.getOrPut(masterIndex, null)
        val copy = result.copy()
        try {
            DiskGroupStore(directory).put(archive, group, copy)
        } finally {
            copy.release()
        }
        return result
    }

    private fun getOpenRs2(
        masterIndex: Js5MasterIndex,
        archive: Int,
        group: Int,
    ): ByteBuf? {
        logger.debug { "Searching for OpenRS2 variant of $archive:$group" }
        val archiveResult =
            resolveOpenRs2(
                masterIndex,
                archive,
                group,
            )
        if (archiveResult != null) {
            val (id, buffer) = archiveResult
            val directory = diskCacheDictionary.getOrPut(masterIndex, id)
            val copy = buffer.copy()
            try {
                DiskGroupStore(directory).put(archive, group, copy)
            } finally {
                copy.release()
            }
            logger.debug { "OpenRS2 variant found for $archive:$group" }
            return buffer
        }
        logger.debug {
            "OpenRS2 variant of $archive:$group not found."
        }
        return null
    }

    private fun resolveLocal(
        masterIndex: Js5MasterIndex,
        archive: Int,
        group: Int,
    ): ByteBuf? {
        return try {
            val path =
                diskCacheIdentifier.identify(masterIndex)
                    ?: return null
            DiskGroupStore(path).get(archive, group)
        } catch (e: Exception) {
            logger.warn(e) {
                "Unable to resolve local cache: $archive, $group, $masterIndex"
            }
            null
        }
    }

    private fun resolveOpenRs2(
        masterIndex: Js5MasterIndex,
        archive: Int,
        group: Int,
    ): Pair<Int, ByteBuf>? {
        return try {
            val id =
                openrs2CacheIdentifier.identify(masterIndex)
                    ?: return null
            val result =
                OpenRs2GroupStore(id).get(archive, group)
                    ?: return null
            id to result
        } catch (e: Exception) {
            logger.warn(e) {
                "Unable to resolve openrs2 cache: $archive, $group, $masterIndex"
            }
            null
        }
    }

    private companion object {
        private val logger = InlineLogger()
    }
}
