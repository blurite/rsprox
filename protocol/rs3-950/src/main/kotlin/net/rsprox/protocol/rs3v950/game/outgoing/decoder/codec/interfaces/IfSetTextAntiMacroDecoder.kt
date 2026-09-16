package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.interfaces.IfSetTextAntiMacro
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class IfSetTextAntiMacroDecoder : ProxyMessageDecoder<IfSetTextAntiMacro> {
    override val prot: ClientProt = GameServerProt.IF_SETTEXTANTIMACRO

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): IfSetTextAntiMacro {
        val componentHash = buffer.g4Alt3().toLong() and 0xFFFF_FFFFL
        val enabled = buffer.g1Alt1() == 1
        return IfSetTextAntiMacro(
            componentHash,
            enabled,
        )
    }
}
