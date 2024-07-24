package net.rsprox.cache.store

import io.netty.buffer.ByteBuf

public interface GroupStore {
    public fun get(
        archive: Int,
        group: Int,
    ): ByteBuf?
}
