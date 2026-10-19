package net.rsprox.protocol.rs3v951.game.outgoing.decoder.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.interfaces.IfOpenSubActiveNpc
import net.rsprox.protocol.rs3v951.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class IfOpenSubActiveNpcDecoder : ProxyMessageDecoder<IfOpenSubActiveNpc> {
    override val prot: ClientProt = GameServerProt.IF_OPENSUB_ACTIVE_NPC

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): IfOpenSubActiveNpc {
        // The native handler skips these fixed spans; preserve bytes without inventing XTEA transforms.
        val childId = buffer.g2Alt3()
        val reserved0 = List(8) { buffer.g1() }
        val componentHash = buffer.g4().toLong() and 0xFFFF_FFFFL
        val reserved1 = List(4) { buffer.g1() }
        val npcIndex = buffer.g2Alt3()
        val reserved2 = List(4) { buffer.g1() }
        val layer = buffer.g1Alt1()
        return IfOpenSubActiveNpc(
            legacyWord0 = null,
            npcIndex = npcIndex,
            childId = childId,
            legacyWord1 = null,
            componentHash = componentHash,
            legacyWord2 = null,
            legacyWord3 = null,
            layer = layer,
            reserved = reserved0 + reserved1 + reserved2,
        )
    }
}
