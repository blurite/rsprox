package net.rsprox.proxy.cache

import net.rsprox.cache.Js5MasterIndex
import net.rsprox.cache.OldSchoolCache
import net.rsprox.cache.api.Cache
import net.rsprox.cache.resolver.CacheResolver

public class CachedCaches(
    private val resolver: CacheResolver,
) {
    private val cachedCaches: MutableMap<Js5MasterIndex, Cache> = mutableMapOf()

    public fun add(masterIndex: Js5MasterIndex) {
        val existing = cachedCaches[masterIndex]
        if (existing != null) {
            return
        }
        val next = OldSchoolCache(resolver, masterIndex)
        this.cachedCaches[masterIndex] = next
    }

    public fun get(masterIndex: Js5MasterIndex): Cache {
        return cachedCaches.getValue(masterIndex)
    }
}
