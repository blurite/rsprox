package net.rsprox.cache.resolver

import com.github.michaelbull.logging.InlineLogger
import io.netty.buffer.ByteBuf
import net.rsprox.cache.CACHES_DIRECTORY
import net.rsprox.cache.DISK_CACHE_DICTIONARY_PATH
import net.rsprox.cache.Js5MasterIndex
import net.rsprox.cache.dictionary.DiskCacheDictionary
import net.rsprox.cache.store.DiskGroupStore
import net.rsprox.cache.store.HistoricGroupCache
import net.rsprox.cache.store.OpenRs2GroupStore
import net.rsprox.cache.util.CacheGroupRequest
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors

public class HistoricCacheResolver : CacheResolver {
    private val dictionary = DiskCacheDictionary.create(DISK_CACHE_DICTIONARY_PATH, CACHES_DIRECTORY)

    override fun get(
        masterIndex: Js5MasterIndex,
        archive: Int,
        group: Int,
    ): ByteBuf? {
        val request = CacheGroupRequest(archive, group)
        return getBulk(masterIndex, listOf(request))[request]
    }

    override fun getBulk(
        masterIndex: Js5MasterIndex,
        requests: List<CacheGroupRequest>,
    ): Map<CacheGroupRequest, ByteBuf> {
        val local = dictionary.getDirectory(masterIndex)?.let(::DiskGroupStore)
        return buildMap {
            val missing = requests.distinct().filter { request ->
                val group = local?.get(request.archive, request.group) ?: return@filter true
                put(request, group)
                false
            }
            if (missing.isEmpty()) return@buildMap

            HistoricGroupCache(masterIndex, local, dictionary.getEntry(masterIndex)?.openrs2Id).use { cache ->
                val store = OpenRs2GroupStore(masterIndex, cache::read)
                val jobs = missing.associateWith { request ->
                    CompletableFuture.supplyAsync({
                        try {
                            store.get(request.archive, request.group)
                        } catch (e: Exception) {
                            logger.warn(e) { "Unable to resolve historic group ${request.archive}:${request.group}" }
                            null
                        }
                    }, workers)
                }
                for ((request, job) in jobs) {
                    job.join()?.let { put(request, it) }
                }
            }
        }
    }

    private companion object {
        private val logger = InlineLogger()
        private val workers = Executors.newFixedThreadPool(4) { task ->
            Thread(task, "osrs-historic-cache").apply { isDaemon = true }
        }
    }
}
