package net.rsprox.protocol.v226.game.incoming.decoder.codec.messaging

import net.rsprot.buffer.JagByteBuf
import net.rsprot.compression.HuffmanCodec
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.incoming.model.messaging.MessagePublic
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v226.game.incoming.decoder.prot.GameClientProt

@Consistent
internal class MessagePublicDecoder(
    private val huffmanCodec: HuffmanCodec,
) : ProxyMessageDecoder<MessagePublic> {
    override val prot: ClientProt = GameClientProt.MESSAGE_PUBLIC

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): MessagePublic {
        val type = buffer.g1()
        val colour = buffer.g1()
        val effect = buffer.g1()
        val patternArray =
            if (colour in 13..20) {
                ByteArray(colour - 12) {
                    buffer.g1().toByte()
                }
            } else {
                null
            }
        val hasTrailingByte = type == CLAN_MAIN_CHANNEL_TYPE
        val huffmanSlice =
            if (hasTrailingByte) {
                buffer.buffer.readSlice(buffer.readableBytes() - 1)
            } else {
                buffer.buffer
            }
        val message = huffmanCodec.decode(huffmanSlice)
        val clanType =
            if (hasTrailingByte) {
                buffer.g1()
            } else {
                -1
            }
        val pattern =
            if (patternArray != null) {
                MessagePublic.MessageColourPattern(patternArray)
            } else {
                null
            }
        return MessagePublic(
            type,
            colour,
            effect,
            message,
            pattern,
            clanType,
        )
    }

    private companion object {
        private const val CLAN_MAIN_CHANNEL_TYPE: Int = 3
    }
}
