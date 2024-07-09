package net.rsprox.protocol.game.incoming.decoder.codec.misc.user

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.tools.MessageDecodingTools
import net.rsprox.protocol.game.incoming.decoder.prot.GameClientProt
import net.rsprox.protocol.game.incoming.model.misc.user.MoveGameClick

public class MoveGameClickDecoder : MessageDecoder<MoveGameClick> {
    override val prot: ClientProt = GameClientProt.MOVE_GAMECLICK

    override fun decode(
        buffer: JagByteBuf,
        tools: MessageDecodingTools,
    ): MoveGameClick {
        val z = buffer.g2Alt1()
        val keyCombination = buffer.g1()
        val x = buffer.g2Alt2()
        return MoveGameClick(
            x,
            z,
            keyCombination,
        )
    }
}
