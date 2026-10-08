package net.rsprox.protocol.rs3v950beta

import net.rsprot.buffer.JagByteBuf
import net.rsprot.compression.HuffmanCodec
import net.rsprot.protocol.message.IncomingMessage
import net.rsprox.protocol.ClientPacketDecoder
import net.rsprox.protocol.rs3v950beta.game.incoming.decoder.prot.ClientMessageDecoderRepository
import net.rsprox.protocol.session.Session

public class ClientPacketDecoderServiceRs3V950Beta(
    huffmanCodec: HuffmanCodec,
) : ClientPacketDecoder {
    @OptIn(ExperimentalStdlibApi::class)
    private val repository = ClientMessageDecoderRepository.build(huffmanCodec)

    override fun decode(
        opcode: Int,
        payload: JagByteBuf,
        session: Session,
    ): IncomingMessage {
        val decoder = repository.getDecoder(opcode)
        val message = decoder.decode(payload, session)
        require(payload.readableBytes() == 0) {
            "${decoder.prot} (opcode $opcode) left ${payload.readableBytes()} payload bytes unread"
        }
        return message
    }
}
