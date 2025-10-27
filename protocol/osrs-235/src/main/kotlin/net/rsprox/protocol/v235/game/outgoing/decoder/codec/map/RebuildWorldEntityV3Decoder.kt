package net.rsprox.protocol.v235.game.outgoing.decoder.codec.map

import net.rsprot.buffer.JagByteBuf
import net.rsprot.buffer.bitbuffer.toBitBuf
import net.rsprot.crypto.xtea.XteaKey
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.map.RebuildWorldEntityV3
import net.rsprox.protocol.game.outgoing.model.map.util.BuildArea
import net.rsprox.protocol.game.outgoing.model.map.util.RebuildRegionZone
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.session.getActiveWorld
import net.rsprox.protocol.session.getWorld
import net.rsprox.protocol.v235.game.outgoing.decoder.prot.GameServerProt

@Consistent
internal class RebuildWorldEntityV3Decoder : ProxyMessageDecoder<RebuildWorldEntityV3> {
    override val prot: ClientProt = GameServerProt.REBUILD_WORLDENTITY_V3

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): RebuildWorldEntityV3 {
        val baseX = buffer.g2()
        val baseZ = buffer.g2()
        val xteaCount = buffer.g2()
        val world = session.getWorld(session.getActiveWorld())
        val width = world.sizeX / 8
        val length = world.sizeZ / 8
        val buildArea = BuildArea(4, width, length)
        buffer.buffer.toBitBuf().use { bitBuf ->
            for (level in 0..<4) {
                for (x in 0..<width) {
                    for (z in 0..<length) {
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
        world.baseX = baseX
        world.baseZ = baseZ
        return RebuildWorldEntityV3(
            baseX,
            baseZ,
            buildArea,
            keys,
        )
    }
}
