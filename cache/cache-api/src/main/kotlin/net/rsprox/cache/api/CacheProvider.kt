package net.rsprox.cache.api

public fun interface CacheProvider {
    public fun get(): Cache
}
