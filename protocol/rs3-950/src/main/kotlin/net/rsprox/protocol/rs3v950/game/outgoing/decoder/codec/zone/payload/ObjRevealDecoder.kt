package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.zone.payload

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.zone.payload.ObjReveal
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameZoneProt
import net.rsprox.protocol.session.Session

internal class ObjRevealDecoder : ProxyMessageDecoder<ObjReveal> {
    override val prot: ClientProt = GameZoneProt.OBJ_REVEAL_V2

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): ObjReveal {
        val objId = buffer.g3Alt3()
        val ownerIndex = buffer.g2Alt1()
        val count = buffer.g2()
        val packedCoord = buffer.g1Alt1()

        val xInZone = (packedCoord ushr 4) and 0x7
        val zInZone = packedCoord and 0x7
        return ObjReveal(true, objId, count, ownerIndex, xInZone, zInZone)
    }
}
