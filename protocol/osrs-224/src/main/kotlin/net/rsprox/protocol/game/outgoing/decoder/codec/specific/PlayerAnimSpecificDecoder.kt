package net.rsprox.protocol.game.outgoing.decoder.codec.specific

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.game.outgoing.model.specific.PlayerAnimSpecific
import net.rsprox.protocol.session.Session

public class PlayerAnimSpecificDecoder : ProxyMessageDecoder<PlayerAnimSpecific> {
    override val prot: ClientProt = GameServerProt.PLAYER_ANIM_SPECIFIC

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): PlayerAnimSpecific {
        val delay = buffer.g1Alt1()
        val id = buffer.g2()
        return PlayerAnimSpecific(
            id,
            delay,
        )
    }
}
