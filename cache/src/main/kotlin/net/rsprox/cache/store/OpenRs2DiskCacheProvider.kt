package net.rsprox.cache.store

import com.github.michaelbull.logging.InlineLogger
import net.rsprox.cache.Js5MasterIndex
import net.rsprox.cache.OPENRS2_DISK_CACHES_DIRECTORY
import net.rsprox.cache.dictionary.OpenRs2CacheDictionary
import net.rsprox.cache.dictionary.openrs2.CacheGame
import net.rsprox.cache.dictionary.openrs2.CacheLanguage
import net.rsprox.cache.dictionary.openrs2.CacheScope
import net.rsprox.cache.util.downloadOpenRs2Group
import java.nio.file.Path

public class OpenRs2DiskCacheProvider(
    private val root: Path = OPENRS2_DISK_CACHES_DIRECTORY,
    private val dictionary: OpenRs2CacheDictionary = OpenRs2CacheDictionary(),
) {
    public fun get(masterIndex: Js5MasterIndex): OpenRs2DiskCacheStore? {
        val cacheId = identify(masterIndex) ?: return null
        return OpenRs2DiskCacheStore(cacheId, root.resolve(cacheId.toString()), masterIndex)
    }

    private fun identify(masterIndex: Js5MasterIndex): Int? {
        val scope = CacheScope.RuneScape
        val entries =
            dictionary.list(
                scope,
                CacheGame.OldSchool,
                CacheLanguage.English,
                masterIndex.revision,
            )
        for (entry in entries) {
            val data =
                try {
                    downloadOpenRs2Group(scope, entry.id, MASTER_INDEX, MASTER_INDEX)
                } catch (e: Exception) {
                    logger.debug(e) { "Unable to read OpenRS2 master index for cache ${entry.id}" }
                    continue
                }
            if (masterIndex.data contentEquals data) {
                logger.info { "Matched replay cache revision ${masterIndex.revision} to OpenRS2 disk cache ${entry.id}" }
                return entry.id
            }
        }
        return null
    }

    private companion object {
        private const val MASTER_INDEX: Int = 0xFF
        private val logger = InlineLogger()
    }
}
