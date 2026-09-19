package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.interfaces.IfOpenSubActiveLoc
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class IfOpenSubActiveLocDecoder : ProxyMessageDecoder<IfOpenSubActiveLoc> {
    override val prot: ClientProt = GameServerProt.IF_OPENSUB_ACTIVE_LOC

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): IfOpenSubActiveLoc {
        val rawShapeRot = buffer.g1()
        val shape = (rawShapeRot ushr 2) and 0x1F
        val rotation = rawShapeRot and 0x3

        val componentHash = buffer.g4Alt3().toLong() and 0xFFFFFFFFL
        val extra1 = buffer.g4Alt2()
        val layer = buffer.g1Alt1()
        val coord = buffer.g4Alt2()
        val locId = buffer.g4()
        val extra2 = buffer.g4Alt2()
        val childId = buffer.g2Alt2()
        val extra3 = buffer.g4Alt3()
        val extra4 = buffer.g4Alt1()

        return IfOpenSubActiveLoc(
            componentHash = componentHash,
            locId = locId,
            childId = childId,
            layer = layer,
            shape = shape,
            rotation = rotation,
            coord = coord,
            extra1 = extra1,
            extra2 = extra2,
            extra3 = extra3,
            extra4 = extra4,
        )
    }
}
