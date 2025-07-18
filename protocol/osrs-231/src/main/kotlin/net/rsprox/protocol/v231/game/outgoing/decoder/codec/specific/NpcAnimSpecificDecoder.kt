package net.rsprox.protocol.v231.game.outgoing.decoder.codec.specific

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.specific.NpcAnimSpecific
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v231.game.outgoing.decoder.prot.GameServerProt

internal class NpcAnimSpecificDecoder : ProxyMessageDecoder<NpcAnimSpecific> {
    override val prot: ClientProt = GameServerProt.NPC_ANIM_SPECIFIC

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): NpcAnimSpecific {
        val delay = buffer.g1()
        val index = buffer.g2()
        val id = buffer.g2Alt3()
        return NpcAnimSpecific(
            index,
            id,
            delay,
        )
    }
}
