package net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.interfaces.IfSetTextAntiMacro
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class IfSetTextAntiMacroDecoder : ProxyMessageDecoder<IfSetTextAntiMacro> {
    override val prot: ClientProt = GameServerProt.IF_SETTEXTANTIMACRO

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): IfSetTextAntiMacro {
        val enabled = buffer.g1Alt2() == 1
        val componentHash = buffer.g4Alt3().toLong() and 0xFFFF_FFFFL
        return IfSetTextAntiMacro(
            componentHash,
            enabled,
        )
    }
}
