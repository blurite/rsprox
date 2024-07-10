package net.rsprox.protocol.game.outgoing.decoder.codec.misc.player

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.tools.MessageDecodingTools
import net.rsprox.protocol.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.game.outgoing.model.misc.player.ChatFilterSettings

public class ChatFilterSettingsDecoder : MessageDecoder<ChatFilterSettings> {
    override val prot: ClientProt = GameServerProt.CHAT_FILTER_SETTINGS

    override fun decode(
        buffer: JagByteBuf,
        tools: MessageDecodingTools,
    ): ChatFilterSettings {
        val tradeChatFilter = buffer.g1Alt3()
        val publicChatFilter = buffer.g1()
        return ChatFilterSettings(
            publicChatFilter,
            tradeChatFilter,
        )
    }
}
