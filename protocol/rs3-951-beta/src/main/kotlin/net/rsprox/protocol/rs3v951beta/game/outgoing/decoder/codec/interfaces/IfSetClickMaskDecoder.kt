package net.rsprox.protocol.rs3v951beta.game.outgoing.decoder.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.interfaces.IfSetClickMask
import net.rsprox.protocol.rs3v951beta.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class IfSetClickMaskDecoder : ProxyMessageDecoder<IfSetClickMask> {
    override val prot: ClientProt = GameServerProt.IF_SETCLICKMASK

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): IfSetClickMask {
        val componentHash = buffer.g4Alt3().toLong() and 0xFFFF_FFFFL
        val enabled = buffer.g1Alt1() == 1
        return IfSetClickMask(
            enabled,
            componentHash,
        )
    }
}
