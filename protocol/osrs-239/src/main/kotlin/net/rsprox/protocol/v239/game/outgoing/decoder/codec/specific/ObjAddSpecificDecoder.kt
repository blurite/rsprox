package net.rsprox.protocol.v239.game.outgoing.decoder.codec.specific

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.common.CoordGrid
import net.rsprox.protocol.game.outgoing.model.specific.ObjAddSpecific
import net.rsprox.protocol.game.outgoing.model.util.OpFlags
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v239.game.outgoing.decoder.prot.GameServerProt

internal class ObjAddSpecificDecoder : ProxyMessageDecoder<ObjAddSpecific> {
    override val prot: ClientProt = GameServerProt.OBJ_ADD_SPECIFIC

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): ObjAddSpecific {
        val timeUntilPublic = buffer.g2()
        val id = buffer.g2Alt1()
        val timeUntilDespawn = buffer.g2Alt1()
        val neverBecomesPublic = buffer.g1Alt3() == 1
        val ownershipType = buffer.g1()
        val opFlags = OpFlags(buffer.g1Alt2())
        val coordGrid = CoordGrid(buffer.g4Alt2())
        val quantity = buffer.g4Alt3()
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
