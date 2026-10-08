package net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.misc.player

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.misc.player.ChatFilterSettings
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class ChatFilterSettingsDecoder : ProxyMessageDecoder<ChatFilterSettings> {
    override val prot: ClientProt = GameServerProt.CHAT_FILTER_SETTINGS

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): ChatFilterSettings {
        val filterSlot0 = buffer.g1Alt2()
        val filterSlot1 = buffer.g1()
        return ChatFilterSettings(
            filterSlot1,
            filterSlot0,
        )
    }
}
