package net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.info.playerinfo

import net.rsprot.buffer.JagByteBuf
import net.rsprox.protocol.rs3v949.game.outgoing.model.info.playerinfo.PlayerInfo
import net.rsprox.protocol.rs3v949.game.outgoing.model.info.playerinfo.util.PlayerInfoInitBlock

public interface PlayerInfoDecoder {
    public fun getOwnIndex(): Int?

    public fun applyOwnIndex(index: Int)

    public fun gpiInit(initBlock: PlayerInfoInitBlock)

    public fun reset()

    public fun decode(buffer: JagByteBuf): PlayerInfo
}
