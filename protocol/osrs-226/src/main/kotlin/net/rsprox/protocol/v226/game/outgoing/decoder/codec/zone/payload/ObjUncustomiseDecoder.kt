package net.rsprox.protocol.v226.game.outgoing.decoder.codec.zone.payload

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.zone.payload.ObjUncustomise
import net.rsprox.protocol.game.outgoing.model.zone.payload.util.CoordInZone
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v226.game.outgoing.decoder.prot.GameServerProt

internal class ObjUncustomiseDecoder : ProxyMessageDecoder<ObjUncustomise> {
    override val prot: ClientProt = GameServerProt.OBJ_UNCUSTOMISE

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): ObjUncustomise {
        val coordInZone = CoordInZone(buffer.g1())
        val quantity = buffer.g4Alt1()
        val id = buffer.g2Alt2()
        return ObjUncustomise(
            id,
            quantity,
            coordInZone.xInZone,
            coordInZone.zInZone,
        )
    }
}
