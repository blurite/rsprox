package net.rsprox.protocol.v223.game.outgoing.decoder.codec.specific

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.specific.MapAnimSpecific
import net.rsprox.protocol.game.outgoing.model.zone.payload.util.CoordInBuildArea
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v223.game.outgoing.decoder.prot.GameServerProt

public class MapAnimSpecificDecoder : ProxyMessageDecoder<MapAnimSpecific> {
    override val prot: ClientProt = GameServerProt.MAP_ANIM_SPECIFIC

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): MapAnimSpecific {
        val id = buffer.g2Alt3()
        val delay = buffer.g2Alt1()
        val coordInBuildArea = CoordInBuildArea(buffer.g3Alt1())
        val height = buffer.g1Alt3()
        return MapAnimSpecific(
            id,
            delay,
            height,
            coordInBuildArea,
        )
    }
}
