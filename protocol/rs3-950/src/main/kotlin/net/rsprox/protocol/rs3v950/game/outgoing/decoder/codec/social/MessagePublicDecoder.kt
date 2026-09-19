package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.social

import net.rsprot.buffer.JagByteBuf
import net.rsprot.compression.HuffmanCodec
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.social.MessagePublic
import net.rsprox.protocol.rs3v950.buffer.readNativeHuffman
import net.rsprox.protocol.rs3v950.cache.readQuickChat
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class MessagePublicDecoder(
    private val huffmanCodec: HuffmanCodec,
) : ProxyMessageDecoder<MessagePublic> {
    override val prot: ClientProt = GameServerProt.MESSAGE_PUBLIC

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): MessagePublic {
        val playerIndex = buffer.g2()
        val colourEffectAndQuickFlag = buffer.g2()
        val playerType = buffer.g1()
        if (colourEffectAndQuickFlag and 32768 != 0) {
            val phraseId = buffer.g2()
            return MessagePublic(
                playerIndex,
                colourEffectAndQuickFlag,
                playerType,
                null,
                phraseId,
                buffer.readQuickChat(session, phraseId),
            )
        }
        return MessagePublic(playerIndex, colourEffectAndQuickFlag, playerType, buffer.readNativeHuffman(huffmanCodec))
    }
}
