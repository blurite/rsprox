package net.rsprox.protocol.v226.game.outgoing.decoder.codec.friendchat

import net.rsprot.buffer.JagByteBuf
import net.rsprot.compression.Base37
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.friendchat.UpdateFriendChatChannelFull
import net.rsprox.protocol.game.outgoing.model.friendchat.UpdateFriendChatChannelFullV2
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v226.game.outgoing.decoder.prot.GameServerProt

@Consistent
public class UpdateFriendChatChannelFullV2Decoder : ProxyMessageDecoder<UpdateFriendChatChannelFullV2> {
    override val prot: ClientProt = GameServerProt.UPDATE_FRIENDCHAT_CHANNEL_FULL_V2

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): UpdateFriendChatChannelFullV2 {
        if (!buffer.isReadable) {
            return UpdateFriendChatChannelFullV2(UpdateFriendChatChannelFullV2.LeaveUpdate)
        }
        val channelOwner = buffer.gjstr()
        val channelName = Base37.decodeWithCase(buffer.g8())
        val kickRank = buffer.g1s()
        val entryCount = buffer.gSmart1or2null()
        val entries =
            buildList {
                for (i in 0..<entryCount) {
                    val name = buffer.gjstr()
                    val worldId = buffer.g2()
                    val rank = buffer.g1s()
                    val worldName = buffer.gjstr()
                    add(
                        UpdateFriendChatChannelFull.FriendChatEntry(
                            name,
                            worldId,
                            rank,
                            worldName,
                        ),
                    )
                }
            }
        return UpdateFriendChatChannelFullV2(
            UpdateFriendChatChannelFullV2.JoinUpdate(
                channelOwner,
                channelName,
                kickRank,
                entries,
            ),
        )
    }
}
