package net.rsprox.protocol.game.incoming.decoder.codec.friendchat

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.metadata.Consistent
import net.rsprot.protocol.tools.MessageDecodingTools
import net.rsprox.protocol.game.incoming.decoder.prot.GameClientProt
import net.rsprox.protocol.game.incoming.model.friendchat.FriendChatJoinLeave

@Consistent
public class FriendChatJoinLeaveDecoder : MessageDecoder<FriendChatJoinLeave> {
    override val prot: ClientProt = GameClientProt.FRIENDCHAT_JOIN_LEAVE

    override fun decode(
        buffer: JagByteBuf,
        tools: MessageDecodingTools,
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
