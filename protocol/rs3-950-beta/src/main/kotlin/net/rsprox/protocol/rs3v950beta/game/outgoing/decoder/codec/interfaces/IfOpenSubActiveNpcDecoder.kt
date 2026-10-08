package net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.interfaces.IfOpenSubActiveNpc
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class IfOpenSubActiveNpcDecoder : ProxyMessageDecoder<IfOpenSubActiveNpc> {
    override val prot: ClientProt = GameServerProt.IF_OPENSUB_ACTIVE_NPC

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): IfOpenSubActiveNpc {
        // The native handler skips these fixed spans; preserve bytes without inventing XTEA transforms.
        val layer = buffer.g1Alt3()
        val reserved0 = List(12) { buffer.g1() }
        val npcIndex = buffer.g2Alt1()
        val componentHash = buffer.g4Alt2().toLong() and 0xFFFF_FFFFL
        val reserved1 = List(4) { buffer.g1() }
        val childId = buffer.g2Alt3()
        return IfOpenSubActiveNpc(
            legacyWord0 = null,
            npcIndex = npcIndex,
            childId = childId,
            legacyWord1 = null,
            componentHash = componentHash,
            legacyWord2 = null,
            legacyWord3 = null,
            layer = layer,
            reserved = reserved0 + reserved1,
        )
    }
}
