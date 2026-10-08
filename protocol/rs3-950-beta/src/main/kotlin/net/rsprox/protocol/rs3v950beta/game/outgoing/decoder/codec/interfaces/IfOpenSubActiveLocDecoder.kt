package net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.interfaces.IfOpenSubActiveLoc
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class IfOpenSubActiveLocDecoder : ProxyMessageDecoder<IfOpenSubActiveLoc> {
    override val prot: ClientProt = GameServerProt.IF_OPENSUB_ACTIVE_LOC

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): IfOpenSubActiveLoc {
        val reserved0 = List(4) { buffer.g1() }
        val coord = buffer.g4Alt1()
        val reserved1 = List(8) { buffer.g1() }
        val layer = buffer.g1Alt2()
        val locId = buffer.g4Alt1()
        val componentHash = buffer.g4Alt1().toLong() and 0xFFFF_FFFFL
        val rawShapeRot = buffer.g1Alt3()
        require(rawShapeRot and 0x80 == 0) { "Extended location transforms do not fit the fixed beta packet" }
        val shape = (rawShapeRot ushr 2) and 0x1F
        val rotation = rawShapeRot and 0x3
        val reserved2 = List(4) { buffer.g1() }
        val childId = buffer.g2()
        return IfOpenSubActiveLoc(
            componentHash = componentHash,
            locId = locId,
            childId = childId,
            layer = layer,
            shape = shape,
            rotation = rotation,
            coord = coord,
            extra1 = null,
            extra2 = null,
            extra3 = null,
            extra4 = null,
            reserved = reserved0 + reserved1 + reserved2,
        )
    }
}
