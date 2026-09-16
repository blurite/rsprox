package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.interfaces.IfSetPlayerModelOther
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class IfSetPlayerModelOtherDecoder : ProxyMessageDecoder<IfSetPlayerModelOther> {
    override val prot: ClientProt = GameServerProt.IF_SETPLAYERMODEL_OTHER

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): IfSetPlayerModelOther {
        val appearanceHash = buffer.g4Alt2()
        val playerIndex = buffer.g2Alt1()
        val componentHash = buffer.g4().toLong() and 0xFFFF_FFFFL
        return IfSetPlayerModelOther(
            appearanceHash,
            playerIndex,
            componentHash,
        )
    }
}
