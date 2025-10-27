package net.rsprox.protocol.v235.game.outgoing.decoder.codec.zone.payload

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.zone.payload.ObjUncustomise
import net.rsprox.protocol.game.outgoing.model.zone.payload.util.CoordInZone
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v235.game.outgoing.decoder.prot.GameServerProt

internal class ObjUncustomiseDecoder : ProxyMessageDecoder<ObjUncustomise> {
    override val prot: ClientProt = GameServerProt.OBJ_UNCUSTOMISE

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): ObjUncustomise {
        val quantity = buffer.g4Alt2()
        val coordInZone = CoordInZone(buffer.g1())
        val id = buffer.g2()
        return ObjUncustomise(
            id,
            quantity,
            coordInZone.xInZone,
            coordInZone.zInZone,
        )
    }
}
