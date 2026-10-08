package net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.interfaces.IfSetPlayerHeadOther
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class IfSetPlayerHeadOtherDecoder : ProxyMessageDecoder<IfSetPlayerHeadOther> {
    override val prot: ClientProt = GameServerProt.IF_SETPLAYERHEAD_OTHER

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): IfSetPlayerHeadOther {
        val componentHash = buffer.g4Alt1().toLong() and 0xFFFF_FFFFL
        val playerIndex = buffer.g2Alt2()
        val appearanceHash = buffer.g4()
        return IfSetPlayerHeadOther(
            appearanceHash,
            componentHash,
            playerIndex,
        )
    }
}
