package net.rsprox.protocol.v235.game.outgoing.decoder.codec.misc.player

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.misc.player.SetPlayerOp
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v235.game.outgoing.decoder.prot.GameServerProt

internal class SetPlayerOpDecoder : ProxyMessageDecoder<SetPlayerOp> {
    override val prot: ClientProt = GameServerProt.SET_PLAYER_OP

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): SetPlayerOp {
        val id = buffer.g1Alt3()
        val op = buffer.gjstr()
        val priority = buffer.g1Alt2() == 1
        return SetPlayerOp(
            id,
            priority,
            if (op == "null") null else op,
        )
    }
}
