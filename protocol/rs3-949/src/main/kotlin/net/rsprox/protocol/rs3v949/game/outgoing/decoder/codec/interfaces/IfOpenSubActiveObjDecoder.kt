package net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.rs3v949.game.outgoing.model.interfaces.IfOpenSubActiveObj
import net.rsprox.protocol.session.Session

internal class IfOpenSubActiveObjDecoder : ProxyMessageDecoder<IfOpenSubActiveObj> {
    override val prot: ClientProt = GameServerProt.IF_OPEN_SUB_ACTIVE_OBJ

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): IfOpenSubActiveObj {
        val layer = buffer.g1()
        val childId = buffer.g2Alt3()
        val componentHash = buffer.g4Alt3().toLong() and 0xFFFFFFFFL
        val extra1 = buffer.g4()
        val objId = buffer.g4()
        val extra2 = buffer.g4()
        val extra3 = buffer.g2()
        val extra4 = buffer.g4()
        val trailingBytes = ByteArray(buffer.readableBytes()) { buffer.g1().toByte() }
        return IfOpenSubActiveObj(
            componentHash,
            childId,
            objId,
            layer,
            extra1,
            extra2,
            extra3,
            extra4,
            trailingBytes,
        )
    }
}
