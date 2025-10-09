package net.rsprox.protocol.v234.game.outgoing.decoder.codec.specific

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.common.CoordGrid
import net.rsprox.protocol.game.outgoing.model.specific.ObjCountSpecific
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v234.game.outgoing.decoder.prot.GameServerProt

internal class ObjCountSpecificDecoder : ProxyMessageDecoder<ObjCountSpecific> {
    override val prot: ClientProt = GameServerProt.OBJ_COUNT_SPECIFIC

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): ObjCountSpecific {
        val newQuantity = buffer.g4Alt2()
        val id = buffer.g2Alt3()
        val coordGrid = CoordGrid(buffer.g4Alt3())
        val oldQuantity = buffer.g4Alt1()
        return ObjCountSpecific(
            id,
            oldQuantity,
            newQuantity,
            coordGrid,
        )
    }
}
