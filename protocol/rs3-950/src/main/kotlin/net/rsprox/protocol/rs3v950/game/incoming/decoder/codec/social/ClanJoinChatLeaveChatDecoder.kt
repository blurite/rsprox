package net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.social

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.incoming.model.social.ClanJoinChatLeaveChat
import net.rsprox.protocol.rs3v950.buffer.readNativeString
import net.rsprox.protocol.session.Session

internal class ClanJoinChatLeaveChatDecoder(
    override val prot: ClientProt,
) : ProxyMessageDecoder<ClanJoinChatLeaveChat> {
    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): ClanJoinChatLeaveChat {
        // Native CLAN_LEAVECHAT writes only a zero frame length, without a string terminator.
        if (!buffer.isReadable) {
            return ClanJoinChatLeaveChat(null)
        }
        val name = buffer.readNativeString()
        return ClanJoinChatLeaveChat(
            name,
        )
    }
}
