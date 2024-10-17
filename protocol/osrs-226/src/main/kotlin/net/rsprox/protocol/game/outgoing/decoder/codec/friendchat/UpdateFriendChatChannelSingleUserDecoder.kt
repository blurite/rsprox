package net.rsprox.protocol.game.outgoing.decoder.codec.friendchat

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.game.outgoing.model.friendchat.UpdateFriendChatChannelSingleUser
import net.rsprox.protocol.session.Session

@Consistent
public class UpdateFriendChatChannelSingleUserDecoder : ProxyMessageDecoder<UpdateFriendChatChannelSingleUser> {
    override val prot: ClientProt = GameServerProt.UPDATE_FRIENDCHAT_CHANNEL_SINGLEUSER

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): UpdateFriendChatChannelSingleUser {
        val name = buffer.gjstr()
        val worldId = buffer.g2()
        val rank = buffer.g1s()
        return if (rank != -128) {
            val worldName = buffer.gjstr()
            UpdateFriendChatChannelSingleUser(
                UpdateFriendChatChannelSingleUser.AddedFriendChatUser(
                    name,
                    worldId,
                    rank,
                    worldName,
                ),
            )
        } else {
            UpdateFriendChatChannelSingleUser(
                UpdateFriendChatChannelSingleUser.RemovedFriendChatUser(
                    name,
                    worldId,
                ),
            )
        }
    }
}
