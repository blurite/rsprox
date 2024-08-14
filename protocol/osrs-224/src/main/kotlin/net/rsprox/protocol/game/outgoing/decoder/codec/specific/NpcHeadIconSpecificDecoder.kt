package net.rsprox.protocol.game.outgoing.decoder.codec.specific

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.game.outgoing.model.specific.NpcHeadIconSpecific
import net.rsprox.protocol.session.Session

public class NpcHeadIconSpecificDecoder : ProxyMessageDecoder<NpcHeadIconSpecific> {
    override val prot: ClientProt = GameServerProt.NPC_HEADICON_SPECIFIC

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): NpcHeadIconSpecific {
        val index = buffer.g2Alt2()
        val spriteGroup = buffer.g4Alt1()
        val spriteIndex = buffer.g2Alt3()
        val headIconSlot = buffer.g1Alt1()
        return NpcHeadIconSpecific(
            index,
            headIconSlot,
            spriteGroup,
            spriteIndex,
        )
    }
}
