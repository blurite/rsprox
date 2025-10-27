package net.rsprox.protocol.v235.game.outgoing.decoder.codec.specific

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.common.CoordGrid
import net.rsprox.protocol.game.outgoing.model.specific.ObjUncustomiseSpecific
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v235.game.outgoing.decoder.prot.GameServerProt

internal class ObjUncustomiseSpecificDecoder : ProxyMessageDecoder<ObjUncustomiseSpecific> {
    override val prot: ClientProt = GameServerProt.OBJ_UNCUSTOMISE_SPECIFIC

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): ObjUncustomiseSpecific {
        val coordGrid = CoordGrid(buffer.g4())
        val quantity = buffer.g4Alt1()
        val id = buffer.g2Alt2()
        return ObjUncustomiseSpecific(
            id,
            quantity,
            coordGrid,
        )
    }
}
