package net.rsprox.cache.resolver

import com.github.michaelbull.logging.InlineLogger
import io.netty.buffer.ByteBuf
import net.rsprox.cache.CACHES_DIRECTORY
import net.rsprox.cache.DISK_CACHE_DICTIONARY_PATH
import net.rsprox.cache.Js5MasterIndex
import net.rsprox.cache.OPENRS2_DISK_CACHES_DIRECTORY
import net.rsprox.cache.dictionary.DiskCacheDictionary
import net.rsprox.cache.dictionary.OpenRs2CacheDictionary
import net.rsprox.cache.identifier.DiskCacheIdentifier
import net.rsprox.cache.identifier.OpenRs2CacheIdentifier
import net.rsprox.cache.store.DiskGroupStore
import net.rsprox.cache.store.OpenRs2DiskCacheStore
import net.rsprox.cache.util.CacheGroupRequest

public class HistoricCacheResolver : CacheResolver {
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

    override fun get(
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
        logger.debug { "Searching for OpenRS2 variant of $archive:$group" }
        val archiveResult =
            resolveOpenRs2(
                masterIndex,
                archive,
                group,
            )
        if (archiveResult != null) {
            logger.debug { "OpenRS2 variant found for $archive:$group" }
            return archiveResult
        }
        logger.debug {
            "OpenRS2 variant of $archive:$group not found."
        }
        return null
    }

    override fun getBulk(
        masterIndex: Js5MasterIndex,
        requests: List<CacheGroupRequest>,
    ): Map<CacheGroupRequest, ByteBuf> {
        return buildMap {
            val remainingRequests = requests.toMutableList()
            // First, run a pass over local ones; these will load quick so there's no point in multithreading
            val localIterator = remainingRequests.iterator()
            while (localIterator.hasNext()) {
                val request = localIterator.next()
                val local = resolveLocal(masterIndex, request.archive, request.group)
                if (local != null) {
                    put(request, local)
                    localIterator.remove()
                }
            }

            if (remainingRequests.isNotEmpty()) {
                val openrs2Bulk = resolveOpenRs2Bulk(masterIndex, remainingRequests)
                remainingRequests.removeAll(openrs2Bulk.keys)
                putAll(openrs2Bulk)
            }

            if (remainingRequests.isNotEmpty()) {
                logger.warn { "Unable to resolve all groups in bulk request; missing groups: $remainingRequests" }
            }
        }
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
    ): ByteBuf? {
        return try {
            val id =
                openrs2CacheIdentifier.identify(masterIndex)
                    ?: return null
            val directory = OPENRS2_DISK_CACHES_DIRECTORY.resolve(id.toString())
            OpenRs2DiskCacheStore(id, directory, masterIndex).get(archive, group)
        } catch (e: Exception) {
            logger.warn(e) {
                "Unable to resolve openrs2 cache: $archive, $group, $masterIndex"
            }
            null
        }
    }

    private fun resolveOpenRs2Bulk(
        masterIndex: Js5MasterIndex,
        requests: List<CacheGroupRequest>,
    ): Map<CacheGroupRequest, ByteBuf> {
        return try {
            val cacheId =
                openrs2CacheIdentifier.identify(masterIndex)
                    ?: return emptyMap<CacheGroupRequest, ByteBuf>()
            val directory = OPENRS2_DISK_CACHES_DIRECTORY.resolve(cacheId.toString())
            logger.debug { "Searching for OpenRS2 variant of requests: $requests" }
            val store = OpenRs2DiskCacheStore(cacheId, directory, masterIndex)
            val responses = store.getBulk(requests.map { it.archive to it.group })
            buildMap {
                for (request in requests) {
                    val buffer = responses[request.archive to request.group] ?: continue
                    logger.debug { "OpenRS2 variant found for ${request.archive}:${request.group}" }
                    put(request, buffer)
                }
            }
        } catch (e: Exception) {
            logger.warn(e) {
                "Unable to resolve openrs2 cache in bulk: $requests, $masterIndex"
            }
            emptyMap()
        }
    }

    private companion object {
        private val logger = InlineLogger()
    }
}
