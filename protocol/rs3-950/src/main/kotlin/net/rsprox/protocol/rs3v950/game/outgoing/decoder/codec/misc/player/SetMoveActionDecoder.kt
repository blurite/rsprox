package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.misc.player

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.misc.player.SetMoveAction
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class SetMoveActionDecoder : ProxyMessageDecoder<SetMoveAction> {
    override val prot: ClientProt = GameServerProt.SET_MOVEACTION

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): SetMoveAction {
        val length = buffer.readableBytes()
        require(length == 0 || length in 2..255) { "Invalid move-action payload length: $length" }
        val action = if (length >= 3) buffer.gjstr() else null
        val cursor = if (length == 0) -1 else buffer.g2().let { if (it == 65535) -1 else it }
        return SetMoveAction(
            action,
            cursor,
        )
    }
}
