package net.rsprox.protocol.game.outgoing.decoder.codec.misc.client

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.metadata.Consistent
import net.rsprot.protocol.tools.MessageDecodingTools
import net.rsprox.protocol.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.game.outgoing.model.misc.client.SendPing

@Consistent
public class SendPingDecoder : MessageDecoder<SendPing> {
    override val prot: ClientProt = GameServerProt.SEND_PING

    override fun decode(
        buffer: JagByteBuf,
        tools: MessageDecodingTools,
    ): SendPing {
        val value1 = buffer.g4()
        val value2 = buffer.g4()
        return SendPing(
            value1,
            value2,
        )
    }
}
