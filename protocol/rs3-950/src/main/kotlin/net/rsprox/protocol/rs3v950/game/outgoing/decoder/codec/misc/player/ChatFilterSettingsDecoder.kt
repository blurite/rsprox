package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.misc.player

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.misc.player.ChatFilterSettings
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class ChatFilterSettingsDecoder : ProxyMessageDecoder<ChatFilterSettings> {
    override val prot: ClientProt = GameServerProt.CHAT_FILTER_SETTINGS

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): ChatFilterSettings {
        val filterSlot1 = buffer.g1()
        val filterSlot0 = buffer.g1Alt3()
        return ChatFilterSettings(
            filterSlot1,
            filterSlot0,
        )
    }
}
