package net.rsprox.protocol.game.outgoing.decoder.codec.misc.player

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.game.outgoing.model.misc.player.ChatFilterSettingsPrivateChat
import net.rsprox.protocol.session.Session

@Consistent
public class ChatFilterSettingsPrivateChatDecoder : ProxyMessageDecoder<ChatFilterSettingsPrivateChat> {
    override val prot: ClientProt = GameServerProt.CHAT_FILTER_SETTINGS_PRIVATECHAT

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): ChatFilterSettingsPrivateChat {
        val privateChatFilter = buffer.g1()
        return ChatFilterSettingsPrivateChat(
            privateChatFilter,
        )
    }
}
