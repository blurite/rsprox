package net.rsprox.protocol.game.outgoing.decoder.codec.friendchat

import net.rsprot.buffer.JagByteBuf
import net.rsprot.compression.Base37
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.metadata.Consistent
import net.rsprot.protocol.tools.MessageDecodingTools
import net.rsprox.protocol.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.game.outgoing.model.friendchat.UpdateFriendChatChannelFull
import net.rsprox.protocol.game.outgoing.model.friendchat.UpdateFriendChatChannelFullV1

@Consistent
public class UpdateFriendChatChannelFullV1Decoder : MessageDecoder<UpdateFriendChatChannelFullV1> {
    override val prot: ClientProt = GameServerProt.UPDATE_FRIENDCHAT_CHANNEL_FULL_V1

    override fun decode(
        buffer: JagByteBuf,
        tools: MessageDecodingTools,
    ): UpdateFriendChatChannelFullV1 {
        if (!buffer.isReadable) {
            return UpdateFriendChatChannelFullV1(UpdateFriendChatChannelFullV1.LeaveUpdate)
        }
        val channelOwner = buffer.gjstr()
        val channelName = Base37.decodeWithCase(buffer.g8())
        val kickRank = buffer.g1()
        val entryCount = buffer.g1s()
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
        return UpdateFriendChatChannelFullV1(
            UpdateFriendChatChannelFullV1.JoinUpdate(
                channelOwner,
                channelName,
                kickRank,
                entries,
            ),
        )
    }
}
