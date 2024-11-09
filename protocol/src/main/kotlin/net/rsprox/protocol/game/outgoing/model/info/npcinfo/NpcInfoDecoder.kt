package net.rsprox.protocol.game.outgoing.model.info.npcinfo

import io.netty.buffer.ByteBuf
import net.rsprox.protocol.common.CoordGrid

public interface NpcInfoDecoder {
    public fun decode(
        buffer: ByteBuf,
        large: Boolean,
        baseCoord: CoordGrid,
    ): NpcInfo
}
