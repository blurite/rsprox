package net.rsprox.protocol.game.outgoing.decoder.codec.misc.player

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.game.outgoing.model.misc.player.ChatFilterSettings
import net.rsprox.protocol.session.Session

public class ChatFilterSettingsDecoder : ProxyMessageDecoder<ChatFilterSettings> {
    override val prot: ClientProt = GameServerProt.CHAT_FILTER_SETTINGS

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): ChatFilterSettings {
        val tradeChatFilter = buffer.g1Alt1()
        val publicChatFilter = buffer.g1()
        return ChatFilterSettings(
            publicChatFilter,
            tradeChatFilter,
        )
    }
}
