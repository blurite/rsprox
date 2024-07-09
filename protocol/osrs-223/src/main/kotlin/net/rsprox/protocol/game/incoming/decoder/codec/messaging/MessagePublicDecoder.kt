package net.rsprox.protocol.game.incoming.decoder.codec.messaging

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.metadata.Consistent
import net.rsprot.protocol.tools.MessageDecodingTools
import net.rsprox.protocol.game.incoming.decoder.prot.GameClientProt
import net.rsprox.protocol.game.incoming.model.messaging.MessagePublic

@Consistent
public class MessagePublicDecoder : MessageDecoder<MessagePublic> {
    override val prot: ClientProt = GameClientProt.MESSAGE_PUBLIC

    override fun decode(
        buffer: JagByteBuf,
        tools: MessageDecodingTools,
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
        val huffman = tools.huffmanCodec.provide()
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
