package net.rsprox.protocol.v234.game.outgoing.decoder.codec.specific

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.specific.PlayerAnimSpecific
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v234.game.outgoing.decoder.prot.GameServerProt

internal class PlayerAnimSpecificDecoder : ProxyMessageDecoder<PlayerAnimSpecific> {
    override val prot: ClientProt = GameServerProt.PLAYER_ANIM_SPECIFIC

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): PlayerAnimSpecific {
        val id = buffer.g2Alt2()
        val delay = buffer.g1Alt2()
        return PlayerAnimSpecific(
            id,
            delay,
        )
    }
}
