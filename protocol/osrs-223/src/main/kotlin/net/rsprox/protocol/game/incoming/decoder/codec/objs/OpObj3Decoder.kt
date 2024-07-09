package net.rsprox.protocol.game.incoming.decoder.codec.objs

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.tools.MessageDecodingTools
import net.rsprox.protocol.game.incoming.decoder.prot.GameClientProt
import net.rsprox.protocol.game.incoming.model.objs.OpObj

public class OpObj3Decoder : MessageDecoder<OpObj> {
    override val prot: ClientProt = GameClientProt.OPOBJ3

    override fun decode(
        buffer: JagByteBuf,
        tools: MessageDecodingTools,
    ): OpObj {
        val z = buffer.g2Alt2()
        val controlKey = buffer.g1Alt1() == 1
        val x = buffer.g2Alt2()
        val id = buffer.g2Alt2()
        return OpObj(
            id,
            x,
            z,
            controlKey,
            3,
        )
    }
}
