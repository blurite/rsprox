package net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.social

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.incoming.model.social.FriendChatJoinLeave
import net.rsprox.protocol.rs3v950.buffer.readNativeString
import net.rsprox.protocol.session.Session

internal class FriendChatJoinLeaveDecoder(
    override val prot: ClientProt,
) : ProxyMessageDecoder<FriendChatJoinLeave> {
    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): FriendChatJoinLeave {
        // Native CLAN_LEAVECHAT writes only a zero frame length, without a string terminator.
        if (!buffer.isReadable) {
            return FriendChatJoinLeave(null)
        }
        val name = buffer.readNativeString()
        return FriendChatJoinLeave(
            name,
        )
    }
}
