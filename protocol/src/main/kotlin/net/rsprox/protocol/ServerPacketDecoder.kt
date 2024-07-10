package net.rsprox.protocol

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.message.IncomingMessage
import net.rsprot.protocol.tools.MessageDecodingTools

public fun interface ServerPacketDecoder {
    public fun decode(
        opcode: Int,
        payload: JagByteBuf,
        decodingTools: MessageDecodingTools,
    ): IncomingMessage
}
