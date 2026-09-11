package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.interfaces.IfSetText
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class IfSetTextDecoder : ProxyMessageDecoder<IfSetText> {
    override val prot: ClientProt = GameServerProt.IF_SETTEXT

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): IfSetText {
        val componentHash = buffer.g4().toLong() and 0xFFFFFFFFL
        val text = buffer.gjstr()
        return IfSetText(
            componentHash,
            text,
        )
    }
}
