package net.rsprox.protocol.game.incoming.decoder.codec.friendchat
import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.incoming.decoder.prot.GameClientProt
import net.rsprox.protocol.game.incoming.model.friendchat.FriendChatKick
import net.rsprox.protocol.session.Session

@Consistent
public class FriendChatKickDecoder : ProxyMessageDecoder<FriendChatKick> {
    override val prot: ClientProt = GameClientProt.FRIENDCHAT_KICK

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): FriendChatKick {
        val name = buffer.gjstr()
        return FriendChatKick(name)
    }
}
