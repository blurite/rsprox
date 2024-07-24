package net.rsprox.proxy.cache

import net.rsprox.cache.Js5MasterIndex
import net.rsprox.cache.OldSchoolCache
import net.rsprox.cache.api.Cache
import net.rsprox.cache.api.CacheProvider
import net.rsprox.cache.resolver.CacheResolver

public class StatefulCacheProvider(
    private val resolver: CacheResolver,
) : CacheProvider {
    private val cachedCaches: MutableMap<Js5MasterIndex, Cache> = mutableMapOf()
    private lateinit var cache: Cache

    override fun get(): Cache {
        return cache
    }

    public fun update(masterIndex: Js5MasterIndex) {
        val existing = cachedCaches[masterIndex]
        if (existing != null) {
            this.cache = existing
            return
        }
        val next = OldSchoolCache(resolver, masterIndex)
        this.cachedCaches[masterIndex] = next
        this.cache = next
    }
}
