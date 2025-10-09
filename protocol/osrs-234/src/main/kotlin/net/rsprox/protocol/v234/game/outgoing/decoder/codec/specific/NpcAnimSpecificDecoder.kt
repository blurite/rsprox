package net.rsprox.protocol.v234.game.outgoing.decoder.codec.specific

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.specific.NpcAnimSpecific
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v234.game.outgoing.decoder.prot.GameServerProt

internal class NpcAnimSpecificDecoder : ProxyMessageDecoder<NpcAnimSpecific> {
    override val prot: ClientProt = GameServerProt.NPC_ANIM_SPECIFIC

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): NpcAnimSpecific {
        val index = buffer.g2Alt2()
        val delay = buffer.g1Alt1()
        val id = buffer.g2()
        return NpcAnimSpecific(
            index,
            id,
            delay,
        )
    }
}
