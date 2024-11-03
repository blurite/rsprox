package net.rsprox.protocol.v223.game.incoming.decoder.codec.misc.user

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.incoming.model.misc.user.SetChatFilterSettings
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v223.game.incoming.decoder.prot.GameClientProt

@Consistent
internal class SetChatFilterSettingsDecoder : ProxyMessageDecoder<SetChatFilterSettings> {
    override val prot: ClientProt = GameClientProt.SET_CHATFILTERSETTINGS

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
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
