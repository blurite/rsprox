package net.rsprox.protocol.game.outgoing.decoder.codec.misc.player

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.tools.MessageDecodingTools
import net.rsprox.protocol.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.game.outgoing.model.misc.player.SetPlayerOp

public class SetPlayerOpDecoder : MessageDecoder<SetPlayerOp> {
    override val prot: ClientProt = GameServerProt.SET_PLAYER_OP

    override fun decode(
        buffer: JagByteBuf,
        tools: MessageDecodingTools,
    ): SetPlayerOp {
        val id = buffer.g1Alt1()
        val priority = buffer.g1Alt3() == 1
        val op = buffer.gjstr()
        return SetPlayerOp(
            id,
            priority,
            if (op == "null") null else op,
        )
    }
}
