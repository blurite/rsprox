package net.rsprox.protocol.game.incoming.decoder.codec.messaging

import net.rsprot.buffer.JagByteBuf
import net.rsprot.compression.HuffmanCodec
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.incoming.decoder.prot.GameClientProt
import net.rsprox.protocol.game.incoming.model.messaging.MessagePublic
import net.rsprox.protocol.session.Session

@Consistent
public class MessagePublicDecoder(
    private val huffman: HuffmanCodec,
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
        val message = huffman.decode(buffer)
        val clanType =
            if (type == CLAN_MAIN_CHANNEL_TYPE) {
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
