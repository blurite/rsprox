package net.rsprox.protocol.v241.game.outgoing.decoder.codec.specific

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.common.CoordGrid
import net.rsprox.protocol.game.outgoing.model.specific.ObjCustomiseSpecific
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v241.game.outgoing.decoder.prot.GameServerProt

internal class ObjCustomiseSpecificDecoder : ProxyMessageDecoder<ObjCustomiseSpecific> {
    override val prot: ClientProt = GameServerProt.OBJ_CUSTOMISE_SPECIFIC

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): ObjCustomiseSpecific {
        val id = buffer.g2Alt1()
        val coordGrid = CoordGrid(buffer.g4Alt1())
        val quantity = buffer.g4Alt3()
        val recol = buffer.g2sAlt3()
        val retex = buffer.g2sAlt3()
        val retexIndex = buffer.g2sAlt2()
        val recolIndex = buffer.g2sAlt3()
        val model = buffer.g2Alt2()
        return ObjCustomiseSpecific(
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
