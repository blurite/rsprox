package net.rsprox.protocol.rs3v950

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.message.IncomingMessage
import net.rsprox.protocol.ServerPacketDecoder
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.ServerMessageDecoderRepository
import net.rsprox.protocol.session.Session

public class ServerPacketDecoderServiceRs3V950 : ServerPacketDecoder {
    @OptIn(ExperimentalStdlibApi::class)
    private val repository = ServerMessageDecoderRepository.build()

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
