package net.rsprox.protocol.v225.game.incoming.decoder.codec.friendchat

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.incoming.model.friendchat.FriendChatJoinLeave
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v225.game.incoming.decoder.prot.GameClientProt

@Consistent
internal class FriendChatJoinLeaveDecoder : ProxyMessageDecoder<FriendChatJoinLeave> {
    override val prot: ClientProt = GameClientProt.FRIENDCHAT_JOIN_LEAVE

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): FriendChatJoinLeave {
        val name =
            if (!buffer.isReadable) {
                null
            } else {
                buffer.gjstr()
            }
        return FriendChatJoinLeave(name)
    }
}
