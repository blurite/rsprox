package net.rsprox.protocol.v224.game.outgoing.decoder.codec.map

import net.rsprot.buffer.JagByteBuf
import net.rsprot.buffer.bitbuffer.toBitBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.common.CoordGrid
import net.rsprox.protocol.game.outgoing.model.info.playerinfo.util.PlayerInfoInitBlock
import net.rsprox.protocol.game.outgoing.model.map.Reconnect
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.session.getWorld
import net.rsprox.protocol.v224.game.outgoing.decoder.prot.GameServerProt

@Suppress("DuplicatedCode")
public class ReconnectDecoder : ProxyMessageDecoder<Reconnect> {
    override val prot: ClientProt = GameServerProt.RECONNECT

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): Reconnect {
        val playerInfoInitBlock =
            buffer.buffer
                .toBitBuf()
                .use { bitBuf ->
                    val localPlayerAbsolutePosition = bitBuf.gBits(30)
                    val lowResolutionPositions = IntArray(2048)
                    for (i in 1..<lowResolutionPositions.size) {
                        if (i == session.localPlayerIndex) continue
                        lowResolutionPositions[i] = bitBuf.gBits(18)
                    }
                    PlayerInfoInitBlock(
                        session.localPlayerIndex,
                        CoordGrid(localPlayerAbsolutePosition),
                        lowResolutionPositions,
                    )
                }

        val world = session.getWorld(-1)
        world.playerInfo.reset()
        world.playerInfo.gpiInit(playerInfoInitBlock)
        return Reconnect(playerInfoInitBlock)
    }
}
