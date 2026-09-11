package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.zone.payload

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.zone.payload.MapAnim
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class MapAnimDecoder : ProxyMessageDecoder<MapAnim> {
    override val prot: ClientProt = GameServerProt.MAP_ANIM

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): MapAnim {
        val packedCoord = buffer.g1()
        val xInZone = (packedCoord ushr 4) and 0x7
        val zInZone = packedCoord and 0x7

        val delay = buffer.g2s()
        val id = buffer.g2()
        val height = buffer.g2Alt1()
        val rotation = buffer.g1()
        buffer.skipRead(3)

        return MapAnim(
            id = id,
            xInZone = xInZone,
            zInZone = zInZone,
            height = height,
            delay = delay,
            rotation = rotation,
        )
    }
}
