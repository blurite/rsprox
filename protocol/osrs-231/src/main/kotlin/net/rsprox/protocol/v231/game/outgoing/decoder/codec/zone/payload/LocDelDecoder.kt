package net.rsprox.protocol.v231.game.outgoing.decoder.codec.zone.payload

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.zone.payload.LocDel
import net.rsprox.protocol.game.outgoing.model.zone.payload.util.CoordInZone
import net.rsprox.protocol.game.outgoing.model.zone.payload.util.LocProperties
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v231.game.outgoing.decoder.prot.GameServerProt

internal class LocDelDecoder : ProxyMessageDecoder<LocDel> {
    override val prot: ClientProt = GameServerProt.LOC_DEL

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): LocDel {
        val coordInZone = CoordInZone(buffer.g1Alt3())
        val locProperties = LocProperties(buffer.g1Alt2())
        return LocDel(
            coordInZone,
            locProperties,
        )
    }
}
