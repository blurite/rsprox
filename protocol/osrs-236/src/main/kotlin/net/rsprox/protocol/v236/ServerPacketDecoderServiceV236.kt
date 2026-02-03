package net.rsprox.protocol.v236

import net.rsprot.buffer.JagByteBuf
import net.rsprot.compression.HuffmanCodec
import net.rsprot.protocol.message.IncomingMessage
import net.rsprox.cache.api.CacheProvider
import net.rsprox.protocol.ServerPacketDecoder
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v236.game.outgoing.decoder.prot.ServerMessageDecoderRepository

public class ServerPacketDecoderServiceV236(
    huffmanCodec: HuffmanCodec,
    cache: CacheProvider,
) : ServerPacketDecoder {
    @OptIn(ExperimentalStdlibApi::class)
    private val repository =
        ServerMessageDecoderRepository.build(
            huffmanCodec,
            cache,
        )

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
