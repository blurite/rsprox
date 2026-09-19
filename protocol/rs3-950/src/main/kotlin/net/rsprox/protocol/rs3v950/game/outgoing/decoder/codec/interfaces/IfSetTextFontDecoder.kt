package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.interfaces.IfSetTextFont
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class IfSetTextFontDecoder : ProxyMessageDecoder<IfSetTextFont> {
    override val prot: ClientProt = GameServerProt.IF_SETTEXTFONT

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): IfSetTextFont {
        val componentHash = buffer.g4Alt2().toLong() and 0xFFFF_FFFFL
        val fontId = buffer.g4Alt3()
        return IfSetTextFont(
            componentHash,
            fontId,
        )
    }
}
