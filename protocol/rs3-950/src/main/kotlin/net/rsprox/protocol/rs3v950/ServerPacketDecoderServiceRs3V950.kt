package net.rsprox.protocol.rs3v950

import net.rsprot.buffer.JagByteBuf
import net.rsprot.compression.HuffmanCodec
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.message.IncomingMessage
import net.rsprox.protocol.ServerPacketDecoder
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.ServerMessageDecoderRepository
import net.rsprox.protocol.session.Session

public class ServerPacketDecoderServiceRs3V950(
    huffmanCodec: HuffmanCodec,
    cipher: () -> StreamCipher? = { null },
) : ServerPacketDecoder {
    @OptIn(ExperimentalStdlibApi::class)
    private val repository = ServerMessageDecoderRepository.build(huffmanCodec, cipher)

    override fun decode(
        opcode: Int,
        payload: JagByteBuf,
        session: Session,
    ): IncomingMessage {
        val inactiveReason =
            when (opcode) {
                GameServerProt.CUTSCENE.opcode,
                -> "Inactive native 950 handler: no payload fields are read; no format is claimed"
                GameServerProt.VORBIS_PRELOAD_SOUND_GROUP.opcode ->
                    "Inactive native 950 handler: cursor skips two bytes without reading a value"
                else -> null
            }
        if (inactiveReason != null) throw UnsupportedOperationException(inactiveReason)
        val decoder = repository.getDecoder(opcode)
        val message = decoder.decode(payload, session)
        require(payload.readableBytes() == 0) {
            "${decoder.prot} (opcode $opcode) left ${payload.readableBytes()} payload bytes unread"
        }
        return message
    }
}
