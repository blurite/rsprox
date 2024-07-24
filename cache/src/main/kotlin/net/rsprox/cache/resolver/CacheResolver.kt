package net.rsprox.cache.resolver

import io.netty.buffer.ByteBuf
import net.rsprox.cache.Js5MasterIndex

public interface CacheResolver {
    public fun get(
        masterIndex: Js5MasterIndex,
        archive: Int,
        group: Int,
    ): ByteBuf?
}
