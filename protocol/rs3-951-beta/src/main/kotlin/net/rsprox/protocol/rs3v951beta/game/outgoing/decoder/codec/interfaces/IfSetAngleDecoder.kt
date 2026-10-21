package net.rsprox.protocol.rs3v951beta.game.outgoing.decoder.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.interfaces.IfSetAngle
import net.rsprox.protocol.rs3v951beta.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class IfSetAngleDecoder : ProxyMessageDecoder<IfSetAngle> {
    override val prot: ClientProt = GameServerProt.IF_SETANGLE

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): IfSetAngle {
        val componentHash = buffer.g4().toLong() and 0xFFFF_FFFFL
        val angle0 = buffer.g2Alt2()
        val angle1 = buffer.g2Alt1()
        val zoom = buffer.g2Alt1()
        return IfSetAngle(
            zoom,
            angle1,
            angle0,
            componentHash,
        )
    }
}
