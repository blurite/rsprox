package net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.rs3v949.game.outgoing.model.interfaces.IfSetText
import net.rsprox.protocol.session.Session

internal class IfSetTextDecoder : ProxyMessageDecoder<IfSetText> {
    override val prot: ClientProt = GameServerProt.IF_SET_TEXT

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): IfSetText {
        val text = buffer.gjstr()
        val componentHash = buffer.g4Alt2().toLong() and 0xFFFFFFFFL
        return IfSetText(
            componentHash,
            text,
        )
    }
}
