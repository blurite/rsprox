package net.rsprox.protocol.rs3v951.game.outgoing.decoder.codec.zone.payload

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.zone.payload.ObjAdd
import net.rsprox.protocol.rs3v951.game.outgoing.decoder.prot.GameZoneProt
import net.rsprox.protocol.session.Session

internal class ObjAddV2Decoder : ProxyMessageDecoder<ObjAdd> {
    override val prot: ClientProt = GameZoneProt.OBJ_ADD_V2

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): ObjAdd {
        val count = buffer.g2Alt2()
        val packedCoord = buffer.g1Alt3()
        val objId = buffer.g3Alt1()
        val xInZone = (packedCoord ushr 4) and 0x7
        val zInZone = packedCoord and 0x7
        return ObjAdd(true, objId, count, xInZone, zInZone, coordinateFlags = packedCoord ushr 7)
    }
}
