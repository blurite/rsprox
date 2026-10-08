package net.rsprox.protocol.rs3v950beta

import net.rsprot.buffer.JagByteBuf
import net.rsprot.compression.HuffmanCodec
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.message.IncomingMessage
import net.rsprox.protocol.ServerPacketDecoder
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.prot.ServerMessageDecoderRepository
import net.rsprox.protocol.session.Session

public class ServerPacketDecoderServiceRs3V950Beta(
    huffmanCodec: HuffmanCodec? = null,
    cipher: () -> StreamCipher? = { null },
) : ServerPacketDecoder {
    @OptIn(ExperimentalStdlibApi::class)
    private val repository = ServerMessageDecoderRepository.build(huffmanCodec, cipher)

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
