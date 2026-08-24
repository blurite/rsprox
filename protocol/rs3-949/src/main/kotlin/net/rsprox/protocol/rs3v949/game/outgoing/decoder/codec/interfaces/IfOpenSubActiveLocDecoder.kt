package net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.rs3v949.game.outgoing.model.interfaces.IfOpenSubActiveLoc
import net.rsprox.protocol.session.Session

internal class IfOpenSubActiveLocDecoder : ProxyMessageDecoder<IfOpenSubActiveLoc> {
    override val prot: ClientProt = GameServerProt.IF_OPEN_SUB_ACTIVE_LOC

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): IfOpenSubActiveLoc {
        val componentHash = buffer.g4Alt2().toLong() and 0xFFFFFFFFL
        val locId = buffer.g4()
        val childId = buffer.g2Alt2()
        val extra1 = buffer.g4()
        val extra2 = buffer.g4()
        val extra3 = buffer.g4()
        val layer = buffer.g1()
        val extra4 = buffer.g4()
        val extra5 = buffer.g4()
        val trailingBytes = ByteArray(buffer.readableBytes()) { buffer.g1().toByte() }
        return IfOpenSubActiveLoc(
            componentHash,
            locId,
            childId,
            layer,
            extra1,
            extra2,
            extra3,
            extra4,
            extra5,
            trailingBytes,
        )
    }
}
