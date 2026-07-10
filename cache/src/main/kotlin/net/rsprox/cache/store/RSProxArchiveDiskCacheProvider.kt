package net.rsprox.cache.store

import com.github.michaelbull.logging.InlineLogger
import net.rsprox.cache.Js5MasterIndex
import net.rsprox.cache.RSPROX_ARCHIVE_DISK_CACHES_DIRECTORY
import net.rsprox.cache.dictionary.RSProxArchiveCacheDictionary
import java.nio.file.Path

public class RSProxArchiveDiskCacheProvider(
    private val root: Path = RSPROX_ARCHIVE_DISK_CACHES_DIRECTORY,
) {
    private val dictionary = RSProxArchiveCacheDictionary()

    public fun get(masterIndex: Js5MasterIndex): RSProxArchiveDiskCacheStore? {
        val entry = dictionary.find(masterIndex) ?: return null
        logger.info {
            "Matched replay cache revision ${masterIndex.revision} to RSProx Archive " +
                "variant ${entry.variant}"
        }
        return RSProxArchiveDiskCacheStore(
            path = root.resolve(entry.masterIndexSha256.lowercase()),
            cacheZipUrl = entry.cacheZipUrl,
            cacheZipSha256 = entry.cacheZipSha256,
            cacheZipSize = entry.cacheZipSize,
        )
    }

    private companion object {
        private val logger = InlineLogger()
    }
}
