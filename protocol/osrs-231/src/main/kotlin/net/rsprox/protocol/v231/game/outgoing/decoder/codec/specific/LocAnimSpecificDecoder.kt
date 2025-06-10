package net.rsprox.protocol.v231.game.outgoing.decoder.codec.specific

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.specific.LocAnimSpecific
import net.rsprox.protocol.game.outgoing.model.zone.payload.util.CoordInBuildArea
import net.rsprox.protocol.game.outgoing.model.zone.payload.util.LocProperties
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v231.game.outgoing.decoder.prot.GameServerProt

internal class LocAnimSpecificDecoder : ProxyMessageDecoder<LocAnimSpecific> {
    override val prot: ClientProt = GameServerProt.LOC_ANIM_SPECIFIC

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): LocAnimSpecific {
        val id = buffer.g2Alt1()
        val locProperties = LocProperties(buffer.g1Alt1())
        val coordInBuildArea = CoordInBuildArea(buffer.g3Alt1())
        return LocAnimSpecific(
            id,
            coordInBuildArea,
            locProperties,
        )
    }
}
