package net.rsprox.protocol.game.incoming.decoder.codec.locs

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.tools.MessageDecodingTools
import net.rsprox.protocol.game.incoming.decoder.prot.GameClientProt
import net.rsprox.protocol.game.incoming.model.locs.OpLoc

public class OpLoc2Decoder : MessageDecoder<OpLoc> {
    override val prot: ClientProt = GameClientProt.OPLOC2

    override fun decode(
        buffer: JagByteBuf,
        tools: MessageDecodingTools,
    ): OpLoc {
        val controlKey = buffer.g1() == 1
        val id = buffer.g2Alt2()
        val x = buffer.g2Alt3()
        val z = buffer.g2()
        return OpLoc(
            id,
            x,
            z,
            controlKey,
            2,
        )
    }
}
