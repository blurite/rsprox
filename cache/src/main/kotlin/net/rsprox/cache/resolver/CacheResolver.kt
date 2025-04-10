package net.rsprox.cache.resolver

import io.netty.buffer.ByteBuf
import net.rsprox.cache.Js5MasterIndex
import net.rsprox.cache.util.CacheGroupRequest

public interface CacheResolver {
    public fun get(
        masterIndex: Js5MasterIndex,
        archive: Int,
        group: Int,
    ): ByteBuf?

    public fun getBulk(
        masterIndex: Js5MasterIndex,
        requests: List<CacheGroupRequest>,
    ): Map<CacheGroupRequest, ByteBuf>
}
