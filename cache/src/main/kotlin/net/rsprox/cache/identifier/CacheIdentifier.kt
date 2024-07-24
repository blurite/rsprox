package net.rsprox.cache.identifier

import net.rsprox.cache.Js5MasterIndex

public fun interface CacheIdentifier<out T> {
    public fun identify(masterIndex: Js5MasterIndex): T?
}
