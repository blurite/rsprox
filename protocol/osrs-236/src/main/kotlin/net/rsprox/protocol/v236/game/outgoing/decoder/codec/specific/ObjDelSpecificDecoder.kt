package net.rsprox.protocol.v236.game.outgoing.decoder.codec.specific

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.common.CoordGrid
import net.rsprox.protocol.game.outgoing.model.specific.ObjDelSpecific
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v236.game.outgoing.decoder.prot.GameServerProt

internal class ObjDelSpecificDecoder : ProxyMessageDecoder<ObjDelSpecific> {
    override val prot: ClientProt = GameServerProt.OBJ_DEL_SPECIFIC

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): ObjDelSpecific {
        val quantity = buffer.g4Alt2()
        val coordGrid = CoordGrid(buffer.g4Alt3())
        val id = buffer.g2Alt1()
        return ObjDelSpecific(
            id,
            quantity,
            coordGrid,
        )
    }
}
