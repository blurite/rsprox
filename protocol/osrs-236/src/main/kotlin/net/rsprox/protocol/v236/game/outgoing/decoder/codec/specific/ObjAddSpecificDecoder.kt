package net.rsprox.protocol.v236.game.outgoing.decoder.codec.specific

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.common.CoordGrid
import net.rsprox.protocol.game.outgoing.model.specific.ObjAddSpecific
import net.rsprox.protocol.game.outgoing.model.util.OpFlags
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v236.game.outgoing.decoder.prot.GameServerProt

internal class ObjAddSpecificDecoder : ProxyMessageDecoder<ObjAddSpecific> {
    override val prot: ClientProt = GameServerProt.OBJ_ADD_SPECIFIC

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): ObjAddSpecific {
        val coordGrid = CoordGrid(buffer.g4Alt1())
        val timeUntilPublic = buffer.g2Alt3()
        val timeUntilDespawn = buffer.g2Alt2()
        val quantity = buffer.g4Alt3()
        val id = buffer.g2Alt1()
        val neverBecomesPublic = buffer.g1Alt3() == 1
        val ownershipType = buffer.g1Alt3()
        val opFlags = OpFlags(buffer.g1Alt1())
        return ObjAddSpecific(
            id,
            quantity,
            coordGrid,
            opFlags,
            timeUntilPublic,
            timeUntilDespawn,
            ownershipType,
            neverBecomesPublic,
        )
    }
}
