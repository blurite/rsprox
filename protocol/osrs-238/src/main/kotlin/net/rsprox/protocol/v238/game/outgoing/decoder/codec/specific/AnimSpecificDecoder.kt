package net.rsprox.protocol.v238.game.outgoing.decoder.codec.specific

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.specific.AnimSpecific
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v238.game.outgoing.decoder.prot.GameServerProt

internal class AnimSpecificDecoder : ProxyMessageDecoder<AnimSpecific> {
    override val prot: ClientProt = GameServerProt.ANIM_SPECIFIC

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): AnimSpecific {
        val delay = buffer.g1()
        val id = buffer.g2Alt3()
        return AnimSpecific(
            id,
            delay,
        )
    }
}
