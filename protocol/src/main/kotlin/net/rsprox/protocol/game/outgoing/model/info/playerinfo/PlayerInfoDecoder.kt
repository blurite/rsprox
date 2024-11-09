package net.rsprox.protocol.game.outgoing.model.info.playerinfo

import io.netty.buffer.ByteBuf
import net.rsprox.protocol.game.outgoing.model.info.playerinfo.util.PlayerInfoInitBlock

public interface PlayerInfoDecoder {
    public fun gpiInit(initBlock: PlayerInfoInitBlock)

    public fun reset()

    public fun decode(buffer: ByteBuf): PlayerInfo
}
