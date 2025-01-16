package net.rsprox.protocol.v228.game.outgoing.decoder.codec.zone.payload

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.zone.payload.MapAnim
import net.rsprox.protocol.game.outgoing.model.zone.payload.util.CoordInZone
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v228.game.outgoing.decoder.prot.GameServerProt

internal class MapAnimDecoder : ProxyMessageDecoder<MapAnim> {
    override val prot: ClientProt = GameServerProt.MAP_ANIM

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): MapAnim {
        val id = buffer.g2()
        val height = buffer.g1Alt1()
        val delay = buffer.g2Alt2()
        val coordInZone = CoordInZone(buffer.g1Alt2())
        return MapAnim(
            id,
            delay,
            height,
            coordInZone,
        )
    }
}
