package net.rsprox.protocol.v224.game.outgoing.decoder.codec.zone.payload

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.zone.payload.ObjCount
import net.rsprox.protocol.game.outgoing.model.zone.payload.util.CoordInZone
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v224.game.outgoing.decoder.prot.GameServerProt

internal class ObjCountDecoder : ProxyMessageDecoder<ObjCount> {
    override val prot: ClientProt = GameServerProt.OBJ_COUNT

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): ObjCount {
        val oldQuantity = buffer.g4Alt2()
        val id = buffer.g2Alt2()
        val newQuantity = buffer.g4Alt1()
        val coordInZone = CoordInZone(buffer.g1Alt2())
        return ObjCount(
            id,
            oldQuantity,
            newQuantity,
            coordInZone,
        )
    }
}
