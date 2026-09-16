package net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.buttons

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.incoming.model.buttons.IfTextChange
import net.rsprox.protocol.rs3v950.buffer.readNativeString
import net.rsprox.protocol.session.Session

internal class IfTextChangeDecoder(
    override val prot: ClientProt,
) : ProxyMessageDecoder<IfTextChange> {
    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): IfTextChange {
        val combinedId = buffer.g4Alt2()
        val sub = buffer.g2Alt2()
        val text = buffer.readNativeString()
        return IfTextChange(
            combinedId,
            sub,
            text,
        )
    }
}
