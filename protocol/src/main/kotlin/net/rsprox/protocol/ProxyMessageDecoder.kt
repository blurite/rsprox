package net.rsprox.protocol

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.message.IncomingMessage
import net.rsprox.protocol.session.Session

public interface ProxyMessageDecoder<out T : IncomingMessage> {
    public val prot: ClientProt

    public fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): T
}
