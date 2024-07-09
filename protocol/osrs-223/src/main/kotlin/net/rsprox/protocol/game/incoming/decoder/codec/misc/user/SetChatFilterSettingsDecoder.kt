package net.rsprox.protocol.game.incoming.decoder.codec.misc.user

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.metadata.Consistent
import net.rsprot.protocol.tools.MessageDecodingTools
import net.rsprox.protocol.game.incoming.decoder.prot.GameClientProt
import net.rsprox.protocol.game.incoming.model.misc.user.SetChatFilterSettings

@Consistent
public class SetChatFilterSettingsDecoder : MessageDecoder<SetChatFilterSettings> {
    override val prot: ClientProt = GameClientProt.SET_CHATFILTERSETTINGS

    override fun decode(
        buffer: JagByteBuf,
        tools: MessageDecodingTools,
    ): SetChatFilterSettings {
        val publicChatFilter = buffer.g1()
        val privateChatFilter = buffer.g1()
        val tradeChatFilter = buffer.g1()
        return SetChatFilterSettings(
            publicChatFilter,
            privateChatFilter,
            tradeChatFilter,
        )
    }
}
