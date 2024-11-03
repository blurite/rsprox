package net.rsprox.protocol.v225.game.outgoing.decoder.codec.map

import net.rsprot.buffer.JagByteBuf
import net.rsprot.buffer.bitbuffer.toBitBuf
import net.rsprot.crypto.xtea.XteaKey
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.map.RebuildWorldEntityV2
import net.rsprox.protocol.game.outgoing.model.map.util.BuildArea
import net.rsprox.protocol.game.outgoing.model.map.util.RebuildRegionZone
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.session.getWorld
import net.rsprox.protocol.v225.game.outgoing.decoder.prot.GameServerProt

@Consistent
internal class RebuildWorldEntityDecoder : ProxyMessageDecoder<RebuildWorldEntityV2> {
    override val prot: ClientProt = GameServerProt.REBUILD_WORLDENTITY

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): RebuildWorldEntityV2 {
        val index = buffer.g2()
        val baseX = buffer.g2()
        val baseZ = buffer.g2()
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
        val world = session.getWorld(index)
        world.baseX = (baseX - 6) * 8
        world.baseZ = (baseZ - 6) * 8
        return RebuildWorldEntityV2(
            index,
            baseX,
            baseZ,
            buildArea,
            keys,
        )
    }
}
