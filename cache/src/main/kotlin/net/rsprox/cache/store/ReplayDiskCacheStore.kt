package net.rsprox.cache.store

public interface ReplayDiskCacheStore :
    GroupStore,
    AutoCloseable {
    public fun open()
}
