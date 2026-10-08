package net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.specific

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.specific.NpcAnimSpecific
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class NpcAnimSpecificDecoder : ProxyMessageDecoder<NpcAnimSpecific> {
    override val prot: ClientProt = GameServerProt.NPC_ANIM_SPECIFIC

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): NpcAnimSpecific {
        val delay = buffer.g1Alt2()
        val npc = buffer.g2Alt1()
        val animation0 = buffer.g4Alt3()
        val animation1 = buffer.g4Alt3()
        val animation2 = buffer.g4Alt3()
        val animation3 = buffer.g4Alt3()
        return NpcAnimSpecific(
            animation0,
            animation1,
            animation2,
            animation3,
            delay,
            npc,
        )
    }
}
