package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.info.npcinfo

import net.rsprot.buffer.JagByteBuf
import net.rsprox.protocol.common.CoordGrid
import net.rsprox.protocol.rs3.game.outgoing.model.info.npcinfo.NpcInfo

public interface NpcInfoDecoder {
    public fun lookupNpcId(index: Int): Int?

    public fun decode(
        buffer: JagByteBuf,
        baseCoord: CoordGrid,
    ): NpcInfo
}
