package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.interfaces.IfOpenSubActivePlayer
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class IfOpenSubActivePlayerDecoder : ProxyMessageDecoder<IfOpenSubActivePlayer> {
    override val prot: ClientProt = GameServerProt.IF_OPENSUB_ACTIVE_PLAYER

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): IfOpenSubActivePlayer {
        val legacyWord0 = buffer.g4Alt2()
        val legacyWord1 = buffer.g4Alt1()
        val playerIndex = buffer.g2()
        val componentHash = buffer.g4Alt3().toLong() and 0xFFFF_FFFFL
        val legacyWord2 = buffer.g4Alt3()
        val legacyWord3 = buffer.g4Alt2()
        val childId = buffer.g2Alt1()
        val layer = buffer.g1Alt3()
        return IfOpenSubActivePlayer(
            legacyWord0 = legacyWord0,
            legacyWord1 = legacyWord1,
            playerIndex = playerIndex,
            componentHash = componentHash,
            legacyWord2 = legacyWord2,
            legacyWord3 = legacyWord3,
            childId = childId,
            layer = layer,
        )
    }
}
