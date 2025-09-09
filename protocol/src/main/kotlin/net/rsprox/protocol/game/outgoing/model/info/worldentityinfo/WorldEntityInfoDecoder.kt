package net.rsprox.protocol.game.outgoing.model.info.worldentityinfo

import net.rsprot.buffer.JagByteBuf
import net.rsprox.protocol.common.CoordGrid

public interface WorldEntityInfoDecoder {
    public fun decode(
        buffer: JagByteBuf,
        baseCoord: CoordGrid,
        version: Int,
    ): WorldEntityInfo
}
