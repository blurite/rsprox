package net.rsprox.protocol

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.message.IncomingMessage
import net.rsprox.protocol.session.Session

public fun interface ServerPacketDecoder {
    public fun decode(
        opcode: Int,
        payload: JagByteBuf,
        session: Session,
    ): IncomingMessage
}
