package net.rsprox.protocol.v230.game.outgoing.decoder.codec.zone.payload

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.util.OpFlags
import net.rsprox.protocol.game.outgoing.model.zone.payload.ObjAdd
import net.rsprox.protocol.game.outgoing.model.zone.payload.util.CoordInZone
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v230.game.outgoing.decoder.prot.GameServerProt

internal class ObjAddDecoder : ProxyMessageDecoder<ObjAdd> {
    override val prot: ClientProt = GameServerProt.OBJ_ADD

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): ObjAdd {
        val quantity = buffer.g4()
        val coordInZone = CoordInZone(buffer.g1())
        val timeUntilDespawn = buffer.g2Alt3()
        val opFlags = OpFlags(buffer.g1Alt3())
        val ownershipType = buffer.g1()
        val id = buffer.g2Alt3()
        val timeUntilPublic = buffer.g2Alt3()
        val neverBecomesPublic = buffer.g1Alt1() == 1
        return ObjAdd(
            id,
            quantity,
            coordInZone,
            opFlags,
            timeUntilPublic,
            timeUntilDespawn,
            ownershipType,
            neverBecomesPublic,
        )
    }
}
