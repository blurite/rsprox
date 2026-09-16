package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.misc.player

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.misc.player.SetTarget
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class SetTargetDecoder : ProxyMessageDecoder<SetTarget> {
    override val prot: ClientProt = GameServerProt.SET_TARGET

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): SetTarget {
        val target = buffer.g3Alt2()
        return SetTarget(
            target,
        )
    }
}
