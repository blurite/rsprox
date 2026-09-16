package net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.chat

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.incoming.model.chat.ChatSetMode
import net.rsprox.protocol.session.Session

internal class ChatSetModeDecoder(
    override val prot: ClientProt,
) : ProxyMessageDecoder<ChatSetMode> {
    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): ChatSetMode {
        val channel = buffer.g1()
        val mode = buffer.g1()
        return ChatSetMode(
            channel,
            mode,
        )
    }
}
