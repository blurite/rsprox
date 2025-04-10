package net.rsprox.cache.store

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import net.rsprox.cache.dictionary.openrs2.CacheScope
import net.rsprox.cache.util.downloadOpenRs2Group

public class OpenRs2GroupStore(
    private val id: Int,
) : GroupStore {
    override fun get(
        archive: Int,
        group: Int,
    ): ByteBuf {
        val result =
            downloadOpenRs2Group(
                CacheScope.RuneScape,
                id,
                archive,
                group,
            )
        return Unpooled.wrappedBuffer(result)
    }
}
