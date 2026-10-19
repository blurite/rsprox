package net.rsprox.protocol.rs3v951.game.outgoing.decoder.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.interfaces.IfSetPlayerModelOther
import net.rsprox.protocol.rs3v951.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class IfSetPlayerModelOtherDecoder : ProxyMessageDecoder<IfSetPlayerModelOther> {
    override val prot: ClientProt = GameServerProt.IF_SETPLAYERMODEL_OTHER

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): IfSetPlayerModelOther {
        val componentHash = buffer.g4().toLong() and 0xFFFF_FFFFL
        val playerIndex = buffer.g2()
        val appearanceHash = buffer.g4Alt2()
        return IfSetPlayerModelOther(
            appearanceHash,
            playerIndex,
            componentHash,
        )
    }
}
