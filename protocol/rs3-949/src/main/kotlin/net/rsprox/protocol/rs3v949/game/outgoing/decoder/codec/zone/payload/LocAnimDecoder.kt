package net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.zone.payload

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.rs3.game.outgoing.model.zone.payload.LocAnim
import net.rsprox.protocol.session.Session

internal class LocAnimDecoder : ProxyMessageDecoder<LocAnim> {
    override val prot: ClientProt = GameServerProt.LOC_ANIM

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): LocAnim {
        val delay = buffer.g1()

        val packedCoord = buffer.g1Alt1()
        val zInZone = packedCoord and 0x7
        val xInZone = (packedCoord ushr 4) and 0x7

        val id = buffer.g4Alt2()

        val rawShapeRot = buffer.g1()
        val shapeRot = rawShapeRot xor 0x80
        val shape = (shapeRot ushr 2) and 0x1F
        val rotation = shapeRot and 0x3

        return LocAnim(id, xInZone, zInZone, shape, rotation, delay)
    }
}
