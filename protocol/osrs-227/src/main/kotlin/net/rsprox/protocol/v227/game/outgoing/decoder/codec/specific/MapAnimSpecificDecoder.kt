package net.rsprox.protocol.v227.game.outgoing.decoder.codec.specific

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.specific.MapAnimSpecific
import net.rsprox.protocol.game.outgoing.model.zone.payload.util.CoordInBuildArea
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v227.game.outgoing.decoder.prot.GameServerProt

internal class MapAnimSpecificDecoder : ProxyMessageDecoder<MapAnimSpecific> {
    override val prot: ClientProt = GameServerProt.MAP_ANIM_SPECIFIC

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): MapAnimSpecific {
        val height = buffer.g1Alt1()
        val id = buffer.g2Alt2()
        val delay = buffer.g2Alt1()
        val coordInBuildArea = CoordInBuildArea(buffer.g3Alt2())
        return MapAnimSpecific(
            id,
            delay,
            height,
            coordInBuildArea,
        )
    }
}
