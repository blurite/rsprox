package net.rsprox.protocol.v224.game.outgoing.decoder.codec.misc.player

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.v224.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.game.outgoing.model.misc.player.MessageGame
import net.rsprox.protocol.session.Session

@Consistent
public class MessageGameDecoder : ProxyMessageDecoder<MessageGame> {
    override val prot: ClientProt = GameServerProt.MESSAGE_GAME

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): MessageGame {
        val type = buffer.gSmart1or2()
        val name =
            if (buffer.g1() == 1) {
                buffer.gjstr()
            } else {
                null
            }
        val message = buffer.gjstr()
        return if (name != null) {
            MessageGame(
                type,
                name,
                message,
            )
        } else {
            MessageGame(
                type,
                message,
            )
        }
    }
}
