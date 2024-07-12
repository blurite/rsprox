package net.rsprox.protocol.game.outgoing.decoder.codec.map

import net.rsprot.buffer.JagByteBuf
import net.rsprot.buffer.bitbuffer.toBitBuf
import net.rsprot.crypto.xtea.XteaKey
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.common.CoordGrid
import net.rsprox.protocol.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.game.outgoing.model.info.playerinfo.util.PlayerInfoInitBlock
import net.rsprox.protocol.game.outgoing.model.map.RebuildWorldEntity
import net.rsprox.protocol.game.outgoing.model.map.util.BuildArea
import net.rsprox.protocol.game.outgoing.model.map.util.RebuildRegionZone
import net.rsprox.protocol.session.Session

@Consistent
public class RebuildWorldEntityDecoder : ProxyMessageDecoder<RebuildWorldEntity> {
    override val prot: ClientProt = GameServerProt.REBUILD_WORLDENTITY

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): RebuildWorldEntity {
        val index = buffer.g2()
        val baseX = buffer.g2()
        val baseZ = buffer.g2()
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
                        CoordGrid(localPlayerAbsolutePosition),
                        lowResolutionPositions,
                    )
                }

        val xteaCount = buffer.g2()
        val buildArea = BuildArea()
        buffer.buffer.toBitBuf().use { bitBuf ->
            for (level in 0..<4) {
                for (x in 0..<13) {
                    for (z in 0..<13) {
                        val exists = bitBuf.gBits(1) == 1
                        val value = if (exists) bitBuf.gBits(26) else -1
                        buildArea[level, x, z] = RebuildRegionZone(value)
                    }
                }
            }
        }
        val keys =
            buildList {
                for (i in 0..<xteaCount) {
                    add(XteaKey(buffer.g4(), buffer.g4(), buffer.g4(), buffer.g4()))
                }
            }
        return RebuildWorldEntity(
            index,
            baseX,
            baseZ,
            buildArea,
            keys,
            playerInfoInitBlock,
        )
    }
}
