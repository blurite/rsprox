package net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.specific

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.specific.PlayerAnimSpecific
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class PlayerAnimSpecificDecoder : ProxyMessageDecoder<PlayerAnimSpecific> {
    override val prot: ClientProt = GameServerProt.PLAYER_ANIM_SPECIFIC

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): PlayerAnimSpecific {
        val animation0 = buffer.g4Alt1()
        val animation1 = buffer.g4Alt1()
        val animation2 = buffer.g4Alt1()
        val animation3 = buffer.g4Alt1()
        val delay = buffer.g1Alt3()
        return PlayerAnimSpecific(
            delay,
            animation0,
            animation1,
            animation2,
            animation3,
        )
    }
}
