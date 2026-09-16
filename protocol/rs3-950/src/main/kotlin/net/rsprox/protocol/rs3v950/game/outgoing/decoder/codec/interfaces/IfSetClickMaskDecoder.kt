package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.interfaces.IfSetClickMask
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class IfSetClickMaskDecoder : ProxyMessageDecoder<IfSetClickMask> {
    override val prot: ClientProt = GameServerProt.IF_SETCLICKMASK

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): IfSetClickMask {
        val enabled = buffer.g1() == 1
        val componentHash = buffer.g4().toLong() and 0xFFFF_FFFFL
        return IfSetClickMask(
            enabled,
            componentHash,
        )
    }
}
