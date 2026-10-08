package net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.zone.payload

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.zone.payload.ObjDel
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class ObjDelDecoder : ProxyMessageDecoder<ObjDel> {
    override val prot: ClientProt = GameServerProt.OBJ_DEL_V2

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): ObjDel {
        val packedCoord = buffer.g1Alt1()
        val objId = buffer.g3Alt3()
        val xInZone = (packedCoord ushr 4) and 0x7
        val zInZone = packedCoord and 0x7
        return ObjDel(true, objId, xInZone, zInZone, coordinateFlags = packedCoord ushr 7)
    }
}
