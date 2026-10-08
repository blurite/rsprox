package net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.zone.payload

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.zone.payload.ObjAdd
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.prot.GameZoneProt
import net.rsprox.protocol.session.Session

internal class ObjAddV2Decoder : ProxyMessageDecoder<ObjAdd> {
    // Six-byte zone payload; the standalone decoder separately consumes its fixed frame tail.
    override val prot: ClientProt = GameZoneProt.OBJ_ADD_V2

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): ObjAdd {
        val count = buffer.g2Alt1()
        val packedCoord = buffer.g1Alt2()
        val objId = buffer.g3Alt3()
        val xInZone = (packedCoord ushr 4) and 0x7
        val zInZone = packedCoord and 0x7
        return ObjAdd(true, objId, count, xInZone, zInZone, coordinateFlags = packedCoord ushr 7)
    }
}
