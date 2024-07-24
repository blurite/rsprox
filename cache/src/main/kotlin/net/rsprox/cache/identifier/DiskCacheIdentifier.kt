package net.rsprox.cache.identifier

import net.rsprox.cache.Js5MasterIndex
import net.rsprox.cache.dictionary.DiskCacheDictionary
import java.nio.file.Path

public class DiskCacheIdentifier(
    private val dictionary: DiskCacheDictionary,
) : CacheIdentifier<Path> {
    override fun identify(masterIndex: Js5MasterIndex): Path? {
        return dictionary.getDirectory(masterIndex)
    }
}
