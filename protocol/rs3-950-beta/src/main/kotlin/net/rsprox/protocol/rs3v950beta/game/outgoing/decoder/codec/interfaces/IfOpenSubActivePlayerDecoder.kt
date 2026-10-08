package net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.interfaces.IfOpenSubActivePlayer
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class IfOpenSubActivePlayerDecoder : ProxyMessageDecoder<IfOpenSubActivePlayer> {
    override val prot: ClientProt = GameServerProt.IF_OPENSUB_ACTIVE_PLAYER

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): IfOpenSubActivePlayer {
        // The native handler skips these fixed spans; preserve bytes without inventing XTEA transforms.
        val reserved0 = List(8) { buffer.g1() }
        val layer = buffer.g1Alt1()
        val playerIndex = buffer.g2Alt3()
        val componentHash = buffer.g4Alt1().toLong() and 0xFFFF_FFFFL
        val reserved1 = List(8) { buffer.g1() }
        val childId = buffer.g2Alt2()
        return IfOpenSubActivePlayer(
            legacyWord0 = null,
            legacyWord1 = null,
            playerIndex = playerIndex,
            componentHash = componentHash,
            legacyWord2 = null,
            legacyWord3 = null,
            childId = childId,
            layer = layer,
            reserved = reserved0 + reserved1,
        )
    }
}
