package net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.social

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.incoming.model.social.FriendListAdd
import net.rsprox.protocol.rs3v950.buffer.readNativeString
import net.rsprox.protocol.session.Session

internal class FriendListAddDecoder(
    override val prot: ClientProt,
) : ProxyMessageDecoder<FriendListAdd> {
    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): FriendListAdd {
        val name = buffer.readNativeString()
        return FriendListAdd(
            name,
        )
    }
}
