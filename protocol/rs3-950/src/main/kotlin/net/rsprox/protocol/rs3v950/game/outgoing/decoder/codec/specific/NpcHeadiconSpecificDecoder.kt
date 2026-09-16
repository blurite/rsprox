package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.specific

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.specific.NpcHeadiconSpecific
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class NpcHeadiconSpecificDecoder : ProxyMessageDecoder<NpcHeadiconSpecific> {
    override val prot: ClientProt = GameServerProt.NPC_HEADICON_SPECIFIC

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): NpcHeadiconSpecific {
        val slot = buffer.g1Alt2()
        val archive = buffer.g4Alt3()
        val npc = buffer.g2Alt1()
        val sprite = buffer.g2Alt2().toShort().toInt()
        return NpcHeadiconSpecific(
            slot,
            archive,
            npc,
            sprite,
        )
    }
}
