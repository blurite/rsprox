package net.rsprox.protocol.v229.game.outgoing.decoder.codec.zone.payload

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.zone.payload.ObjUncustomise
import net.rsprox.protocol.game.outgoing.model.zone.payload.util.CoordInZone
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v229.game.outgoing.decoder.prot.GameServerProt

internal class ObjUncustomiseDecoder : ProxyMessageDecoder<ObjUncustomise> {
    override val prot: ClientProt = GameServerProt.OBJ_UNCUSTOMISE

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): ObjUncustomise {
        val id = buffer.g2Alt1()
        val coordInZone = CoordInZone(buffer.g1Alt3())
        val quantity = buffer.g4Alt1()
        return ObjUncustomise(
            id,
            quantity,
            coordInZone.xInZone,
            coordInZone.zInZone,
        )
    }
}
