package net.rsprox.protocol.game.outgoing.decoder.codec.misc.player

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.metadata.Consistent
import net.rsprot.protocol.tools.MessageDecodingTools
import net.rsprox.protocol.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.game.outgoing.model.misc.player.MessageGame

@Consistent
public class MessageGameDecoder : MessageDecoder<MessageGame> {
    override val prot: ClientProt = GameServerProt.MESSAGE_GAME

    override fun decode(
        buffer: JagByteBuf,
        tools: MessageDecodingTools,
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
