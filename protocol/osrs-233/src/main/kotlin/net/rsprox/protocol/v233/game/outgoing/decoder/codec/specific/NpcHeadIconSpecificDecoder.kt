package net.rsprox.protocol.v233.game.outgoing.decoder.codec.specific

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.specific.NpcHeadIconSpecific
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v233.game.outgoing.decoder.prot.GameServerProt

internal class NpcHeadIconSpecificDecoder : ProxyMessageDecoder<NpcHeadIconSpecific> {
    override val prot: ClientProt = GameServerProt.NPC_HEADICON_SPECIFIC

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): NpcHeadIconSpecific {
        val spriteIndex = buffer.g2Alt1()
        val spriteGroup = buffer.g4Alt3()
        val index = buffer.g2Alt1()
        val headIconSlot = buffer.g1()
        return NpcHeadIconSpecific(
            index,
            headIconSlot,
            spriteGroup,
            spriteIndex,
        )
    }
}
