package net.rsprox.protocol

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.message.IncomingMessage
import net.rsprot.protocol.tools.MessageDecodingTools
import net.rsprox.protocol.game.outgoing.decoder.prot.ServerMessageDecoderRepository

public class ServerPacketDecoderService : ServerPacketDecoder {
    @OptIn(ExperimentalStdlibApi::class)
    private val repository = ServerMessageDecoderRepository.build()

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
