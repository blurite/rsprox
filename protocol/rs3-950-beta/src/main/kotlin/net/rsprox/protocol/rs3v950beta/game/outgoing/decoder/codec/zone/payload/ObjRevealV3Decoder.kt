package net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.zone.payload

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.zone.payload.ObjReveal
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.prot.GameZoneProt
import net.rsprox.protocol.session.Session

internal class ObjRevealV3Decoder : ProxyMessageDecoder<ObjReveal> {
    override val prot: ClientProt = GameZoneProt.OBJ_REVEAL_V3

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): ObjReveal {
        val packedCoord = buffer.g1()
        val objId = buffer.g3()
        val count = buffer.gSmart2or4()
        val excludedPlayerIndex = buffer.g2()
        val xInZone = (packedCoord ushr 4) and 0x7
        val zInZone = packedCoord and 0x7
        return ObjReveal(true, objId, count, excludedPlayerIndex, xInZone, zInZone, version = 3)
    }
}
