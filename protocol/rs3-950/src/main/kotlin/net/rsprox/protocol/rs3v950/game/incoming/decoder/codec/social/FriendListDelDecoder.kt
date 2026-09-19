package net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.social

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.incoming.model.social.FriendListDel
import net.rsprox.protocol.rs3v950.buffer.readNativeString
import net.rsprox.protocol.session.Session

internal class FriendListDelDecoder(
    override val prot: ClientProt,
) : ProxyMessageDecoder<FriendListDel> {
    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): FriendListDel {
        val name = buffer.readNativeString()
        return FriendListDel(
            name,
        )
    }
}
