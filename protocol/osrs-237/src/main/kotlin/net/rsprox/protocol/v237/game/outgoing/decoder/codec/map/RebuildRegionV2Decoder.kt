package net.rsprox.protocol.v237.game.outgoing.decoder.codec.map

import net.rsprot.buffer.JagByteBuf
import net.rsprot.buffer.bitbuffer.toBitBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.map.RebuildRegionV2
import net.rsprox.protocol.game.outgoing.model.map.util.BuildArea
import net.rsprox.protocol.game.outgoing.model.map.util.RebuildRegionZone
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.session.getWorld
import net.rsprox.protocol.v237.game.outgoing.decoder.prot.GameServerProt

internal class RebuildRegionV2Decoder : ProxyMessageDecoder<RebuildRegionV2> {
    override val prot: ClientProt = GameServerProt.REBUILD_REGION_V2

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): RebuildRegionV2 {
        val reload = buffer.g1Alt1() == 1
        val zoneX = buffer.g2()
        val zoneZ = buffer.g2Alt3()

        @Suppress("UnusedVariable", "unused")
        val distinctMapsquareCount = buffer.g2()
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
        val world = session.getWorld(0)
        world.baseX = (zoneX - 6) * 8
        world.baseZ = (zoneZ - 6) * 8
        return RebuildRegionV2(
            zoneX,
            zoneZ,
            reload,
            buildArea,
        )
    }
}
