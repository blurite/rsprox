package net.rsprox.protocol.game.incoming.decoder.codec.friendchat

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.metadata.Consistent
import net.rsprot.protocol.tools.MessageDecodingTools
import net.rsprox.protocol.game.incoming.decoder.prot.GameClientProt
import net.rsprox.protocol.game.incoming.model.friendchat.FriendChatKick

@Consistent
public class FriendChatKickDecoder : MessageDecoder<FriendChatKick> {
    override val prot: ClientProt = GameClientProt.FRIENDCHAT_KICK

    override fun decode(
        buffer: JagByteBuf,
        tools: MessageDecodingTools,
    ): FriendChatKick {
        val name = buffer.gjstr()
        return FriendChatKick(name)
    }
}
