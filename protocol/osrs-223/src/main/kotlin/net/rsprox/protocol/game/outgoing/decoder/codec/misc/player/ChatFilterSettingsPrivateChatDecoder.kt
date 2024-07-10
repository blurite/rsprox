package net.rsprox.protocol.game.outgoing.decoder.codec.misc.player

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.metadata.Consistent
import net.rsprot.protocol.tools.MessageDecodingTools
import net.rsprox.protocol.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.game.outgoing.model.misc.player.ChatFilterSettingsPrivateChat

@Consistent
public class ChatFilterSettingsPrivateChatDecoder : MessageDecoder<ChatFilterSettingsPrivateChat> {
    override val prot: ClientProt = GameServerProt.CHAT_FILTER_SETTINGS_PRIVATECHAT

    override fun decode(
        buffer: JagByteBuf,
        tools: MessageDecodingTools,
    ): ChatFilterSettingsPrivateChat {
        val privateChatFilter = buffer.g1()
        return ChatFilterSettingsPrivateChat(
            privateChatFilter,
        )
    }
}
