package net.rsprox.protocol

import net.rsprot.buffer.JagByteBuf
import net.rsprot.compression.HuffmanCodec
import net.rsprot.protocol.message.IncomingMessage
import net.rsprox.protocol.game.incoming.decoder.prot.ClientMessageDecoderRepository
import net.rsprox.protocol.session.Session

public class ClientPacketDecoderService(
    huffmanCodec: HuffmanCodec,
) : ClientPacketDecoder {
    @OptIn(ExperimentalStdlibApi::class)
    private val repository = ClientMessageDecoderRepository.build(huffmanCodec)

    override fun decode(
        opcode: Int,
        payload: JagByteBuf,
        session: Session,
    ): IncomingMessage {
        return repository
            .getDecoder(opcode)
            .decode(payload, session)
    }
}
