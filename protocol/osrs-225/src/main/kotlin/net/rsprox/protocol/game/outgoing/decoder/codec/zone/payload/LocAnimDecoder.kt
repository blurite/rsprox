package net.rsprox.protocol.game.outgoing.decoder.codec.zone.payload

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.game.outgoing.model.zone.payload.LocAnim
import net.rsprox.protocol.game.outgoing.model.zone.payload.util.CoordInZone
import net.rsprox.protocol.game.outgoing.model.zone.payload.util.LocProperties
import net.rsprox.protocol.session.Session

public class LocAnimDecoder : ProxyMessageDecoder<LocAnim> {
    override val prot: ClientProt = GameServerProt.LOC_ANIM

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): LocAnim {
        val locProperties = LocProperties(buffer.g1Alt3())
        val coordInZone = CoordInZone(buffer.g1())
        val id = buffer.g2Alt1()
        return LocAnim(
            id,
            coordInZone,
            locProperties,
        )
    }
}
