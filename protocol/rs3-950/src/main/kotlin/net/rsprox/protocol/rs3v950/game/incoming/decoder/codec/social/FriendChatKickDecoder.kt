package net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.social

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.incoming.model.social.FriendChatKick
import net.rsprox.protocol.rs3v950.buffer.readNativeString
import net.rsprox.protocol.session.Session

internal class FriendChatKickDecoder(
    override val prot: ClientProt,
) : ProxyMessageDecoder<FriendChatKick> {
    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): FriendChatKick {
        val name = buffer.readNativeString()
        return FriendChatKick(
            name,
        )
    }
}
