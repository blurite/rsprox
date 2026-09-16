package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.misc.player

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.misc.player.SetPlayerAttackPriority
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class SetPlayerAttackPriorityDecoder : ProxyMessageDecoder<SetPlayerAttackPriority> {
    override val prot: ClientProt = GameServerProt.SET_PLAYER_ATTACK_PRIORITY

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): SetPlayerAttackPriority {
        val priority = buffer.g1Alt3()
        return SetPlayerAttackPriority(
            priority,
        )
    }
}
