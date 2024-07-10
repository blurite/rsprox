package net.rsprox.protocol

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.message.IncomingMessage
import net.rsprot.protocol.tools.MessageDecodingTools
import net.rsprox.protocol.game.incoming.decoder.prot.ClientMessageDecoderRepository

public class ClientPacketDecoderService : ClientPacketDecoder {
    @OptIn(ExperimentalStdlibApi::class)
    private val repository = ClientMessageDecoderRepository.build()

    override fun decode(
        opcode: Int,
        payload: JagByteBuf,
        decodingTools: MessageDecodingTools,
    ): IncomingMessage {
        return repository
            .getDecoder(opcode)
            .decode(payload, decodingTools)
    }
}