package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.zone.payload

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.zone.payload.ObjAdd
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class ObjAddDecoder : ProxyMessageDecoder<ObjAdd> {
    override val prot: ClientProt = GameServerProt.OBJ_ADD_V2

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): ObjAdd {
        val objId = buffer.g3Alt1()
        val packedCoord = buffer.g1Alt3()
        val count = buffer.g2()
        val xInZone = (packedCoord ushr 4) and 0x7
        val zInZone = packedCoord and 0x7
        return ObjAdd(true, objId, count, xInZone, zInZone)
    }
}
