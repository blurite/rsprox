package net.rsprox.protocol.rs3v951.game.outgoing.decoder.codec.zone.payload

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.zone.payload.ObjReveal
import net.rsprox.protocol.rs3v951.game.outgoing.decoder.prot.GameZoneProt
import net.rsprox.protocol.session.Session

internal class ObjRevealDecoder : ProxyMessageDecoder<ObjReveal> {
    override val prot: ClientProt = GameZoneProt.OBJ_REVEAL

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): ObjReveal {
        val excludedPlayerIndex = buffer.g2()
        val objId = buffer.g3Alt1()
        val packedCoord = buffer.g1Alt2()
        val count = buffer.g2Alt3()
        val xInZone = (packedCoord ushr 4) and 0x7
        val zInZone = packedCoord and 0x7
        return ObjReveal(true, objId, count, excludedPlayerIndex, xInZone, zInZone)
    }
}
