package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.interfaces.IfOpenSubActiveNpc
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class IfOpenSubActiveNpcDecoder : ProxyMessageDecoder<IfOpenSubActiveNpc> {
    override val prot: ClientProt = GameServerProt.IF_OPENSUB_ACTIVE_NPC

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): IfOpenSubActiveNpc {
        val legacyWord0 = buffer.g4Alt2()
        val npcIndex = buffer.g2Alt2()
        val childId = buffer.g2Alt1()
        val legacyWord1 = buffer.g4()
        val componentHash = buffer.g4Alt3().toLong() and 0xFFFF_FFFFL
        val legacyWord2 = buffer.g4Alt2()
        val legacyWord3 = buffer.g4()
        val layer = buffer.g1Alt2()
        return IfOpenSubActiveNpc(
            legacyWord0 = legacyWord0,
            npcIndex = npcIndex,
            childId = childId,
            legacyWord1 = legacyWord1,
            componentHash = componentHash,
            legacyWord2 = legacyWord2,
            legacyWord3 = legacyWord3,
            layer = layer,
        )
    }
}
