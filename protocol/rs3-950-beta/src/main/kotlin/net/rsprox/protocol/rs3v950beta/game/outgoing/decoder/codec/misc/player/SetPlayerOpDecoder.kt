package net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.misc.player

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.misc.player.SetPlayerOp
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class SetPlayerOpDecoder : ProxyMessageDecoder<SetPlayerOp> {
    override val prot: ClientProt = GameServerProt.SET_PLAYER_OP

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): SetPlayerOp {
        val rawCursor = buffer.g2Alt3()
        val priority = buffer.g1() == 0x80
        val slot = buffer.g1Alt3() - 1
        val text = buffer.gjstr()
        val cursor = if (rawCursor == 0xFFFF) -1 else rawCursor
        return SetPlayerOp(
            slot,
            priority,
            text,
            cursor,
        )
    }
}
