package net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.social

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.incoming.model.social.IgnoreSetNotes
import net.rsprox.protocol.rs3v950.buffer.readNativeString
import net.rsprox.protocol.session.Session

internal class IgnoreSetNotesDecoder(
    override val prot: ClientProt,
) : ProxyMessageDecoder<IgnoreSetNotes> {
    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): IgnoreSetNotes {
        val note = buffer.readNativeString()
        val name = buffer.readNativeString()
        return IgnoreSetNotes(
            note,
            name,
        )
    }
}
