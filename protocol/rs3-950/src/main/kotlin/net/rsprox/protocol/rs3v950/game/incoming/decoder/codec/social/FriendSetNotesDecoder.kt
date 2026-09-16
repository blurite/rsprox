package net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.social

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.incoming.model.social.FriendSetNotes
import net.rsprox.protocol.rs3v950.buffer.readNativeString
import net.rsprox.protocol.session.Session

internal class FriendSetNotesDecoder(
    override val prot: ClientProt,
) : ProxyMessageDecoder<FriendSetNotes> {
    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): FriendSetNotes {
        val name = buffer.readNativeString()
        val note = buffer.readNativeString()
        return FriendSetNotes(
            name,
            note,
        )
    }
}
