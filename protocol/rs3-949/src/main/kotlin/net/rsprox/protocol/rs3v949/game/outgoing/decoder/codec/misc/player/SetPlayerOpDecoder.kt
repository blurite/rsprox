package net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.misc.player

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.rs3v949.game.outgoing.model.misc.player.SetPlayerOp
import net.rsprox.protocol.session.Session

internal class SetPlayerOpDecoder : ProxyMessageDecoder<SetPlayerOp> {
    override val prot: ClientProt = GameServerProt.SET_PLAYER_OP

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): SetPlayerOp {
        val slot = buffer.g1Alt1() - 1
        val priority = buffer.g1() == 0x80
        val text = buffer.gjstr()
        val rawWorld = buffer.g2Alt3()
        val worldId = if (rawWorld == 0xFFFF) -1 else rawWorld
        return SetPlayerOp(
            slot,
            priority,
            text,
            worldId,
        )
    }
}
