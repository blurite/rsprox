package net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.specific

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.specific.NpcHeadiconSpecific
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class NpcHeadiconSpecificDecoder : ProxyMessageDecoder<NpcHeadiconSpecific> {
    override val prot: ClientProt = GameServerProt.NPC_HEADICON_SPECIFIC

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): NpcHeadiconSpecific {
        val id = buffer.g4()
        val slot = buffer.g1Alt3()
        val spriteIndex = buffer.g2().toShort().toInt()
        val npcIndex = buffer.g2Alt3()
        return NpcHeadiconSpecific(
            slot,
            id,
            npcIndex,
            spriteIndex,
        )
    }
}
