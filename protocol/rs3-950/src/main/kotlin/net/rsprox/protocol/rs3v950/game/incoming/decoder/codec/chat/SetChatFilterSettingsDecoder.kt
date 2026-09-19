package net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.chat

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.incoming.model.chat.SetChatFilterSettings
import net.rsprox.protocol.session.Session

internal class SetChatFilterSettingsDecoder(
    override val prot: ClientProt,
) : ProxyMessageDecoder<SetChatFilterSettings> {
    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): SetChatFilterSettings {
        val publicMode = buffer.g1()
        val privateMode = buffer.g1()
        val tradeMode = buffer.g1()
        return SetChatFilterSettings(
            publicMode,
            privateMode,
            tradeMode,
        )
    }
}
