package net.rsprox.cache.identifier

import com.github.michaelbull.logging.InlineLogger
import net.rsprox.cache.Js5MasterIndex
import net.rsprox.cache.dictionary.DiskCacheDictionary
import net.rsprox.cache.dictionary.OpenRs2CacheDictionary
import net.rsprox.cache.dictionary.openrs2.CacheGame
import net.rsprox.cache.dictionary.openrs2.CacheLanguage
import net.rsprox.cache.dictionary.openrs2.CacheScope
import net.rsprox.cache.util.downloadOpenRs2Group

public class OpenRs2CacheIdentifier(
    private val diskCacheDictionary: DiskCacheDictionary,
    private val openRs2CacheDictionary: OpenRs2CacheDictionary,
) : CacheIdentifier<Int> {
    override fun identify(masterIndex: Js5MasterIndex): Int? {
        val local = diskCacheDictionary.getEntry(masterIndex)
        if (local != null) {
            val version = local.openrs2Id
            if (version != null) {
                return version
            }
        }
        val scope = CacheScope.RuneScape
        val list =
            openRs2CacheDictionary.list(
                scope,
                CacheGame.OldSchool,
                CacheLanguage.English,
                masterIndex.revision,
            )
        logger.debug {
            "Attempting to identify cache for revision ${masterIndex.revision}"
        }
        if (list.isEmpty()) {
            logger.debug { "Unable to locate any entries." }
            return null
        }
        var dirty = false
        try {
            for (entry in list) {
                logger.debug {
                    "Attempting to download entry ${entry.id}"
                }
                val data =
                    try {
                        downloadOpenRs2Group(
                            scope,
                            entry.id,
                            MASTER_INDEX,
                            MASTER_INDEX,
                        )
                    } catch (e: Exception) {
                        logger.error(e) {
                            "Unable to resolve ${entry.id}/$scope"
                        }
                        continue
                    }
                logger.debug { "Download successful for master index of entry ${entry.id}" }
                // Store any results, so we don't end up requesting the same stuff repeatedly
                val resolvedMasterIndex = Js5MasterIndex(masterIndex.revision, data)
                if (!diskCacheDictionary.exists(resolvedMasterIndex)) {
                    diskCacheDictionary.store(resolvedMasterIndex, entry.id)
                    dirty = true
                }
                if (masterIndex.data contentEquals data) {
                    return entry.id
                }
            }
        } finally {
            if (dirty) {
                diskCacheDictionary.write()
            }
        }
        return null
    }

    private companion object {
        private const val MASTER_INDEX: Int = 0xFF
        private val logger = InlineLogger()
    }
}
