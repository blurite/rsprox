package net.rsprox.protocol.v237.game.outgoing.decoder.codec.specific

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.common.CoordGrid
import net.rsprox.protocol.game.outgoing.model.specific.ObjCustomiseSpecificV1
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v237.game.outgoing.decoder.prot.GameServerProt

internal class ObjCustomiseSpecificV1Decoder : ProxyMessageDecoder<ObjCustomiseSpecificV1> {
    override val prot: ClientProt = GameServerProt.OBJ_CUSTOMISE_SPECIFIC_V1

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): ObjCustomiseSpecificV1 {
        val recolIndex = buffer.g2sAlt3()
        val quantity = buffer.g4Alt3()
        val retexIndex = buffer.g2s()
        val model = buffer.g2Alt1()
        val recol = buffer.g2sAlt3()
        val retex = buffer.g2s()
        val coordGrid = CoordGrid(buffer.g4())
        val id = buffer.g2Alt1()
        return ObjCustomiseSpecificV1(
            id,
            quantity,
            model,
            recolIndex,
            recol,
            retexIndex,
            retex,
            coordGrid,
        )
    }
}
