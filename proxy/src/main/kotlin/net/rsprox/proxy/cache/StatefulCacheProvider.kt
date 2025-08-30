package net.rsprox.proxy.cache

import net.rsprox.cache.Js5MasterIndex
import net.rsprox.cache.OldSchoolCache
import net.rsprox.cache.api.Cache
import net.rsprox.cache.api.CacheProvider
import net.rsprox.cache.resolver.CacheResolver

public class StatefulCacheProvider(
    private val cached: CachedCaches,
) : CacheProvider {
    private lateinit var masterIndex: Js5MasterIndex

    override fun get(): Cache {
        return cached.get(masterIndex)
    }

    public fun update(masterIndex: Js5MasterIndex) {
        this.masterIndex = masterIndex
        cached.add(masterIndex)
    }
}
