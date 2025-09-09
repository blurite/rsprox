package net.rsprox.protocol.v233.game.outgoing.decoder.codec.map

import net.rsprot.buffer.JagByteBuf
import net.rsprot.buffer.bitbuffer.toBitBuf
import net.rsprot.crypto.xtea.XteaKey
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.map.RebuildRegion
import net.rsprox.protocol.game.outgoing.model.map.util.BuildArea
import net.rsprox.protocol.game.outgoing.model.map.util.RebuildRegionZone
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.session.getWorld
import net.rsprox.protocol.v233.game.outgoing.decoder.prot.GameServerProt

internal class RebuildRegionDecoder : ProxyMessageDecoder<RebuildRegion> {
    override val prot: ClientProt = GameServerProt.REBUILD_REGION

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): RebuildRegion {
        val zoneZ = buffer.g2Alt2()
        val reload = buffer.g1() == 1
        val zoneX = buffer.g2Alt3()
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
        val world = session.getWorld(-1)
        world.baseX = (zoneX - 6) * 8
        world.baseZ = (zoneZ - 6) * 8
        return RebuildRegion(
            zoneX,
            zoneZ,
            reload,
            buildArea,
            keys,
        )
    }
}
