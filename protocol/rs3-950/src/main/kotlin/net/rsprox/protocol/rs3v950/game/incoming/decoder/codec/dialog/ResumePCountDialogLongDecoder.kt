package net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.dialog

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.incoming.model.dialog.ResumePCountDialogLong
import net.rsprox.protocol.session.Session

internal class ResumePCountDialogLongDecoder(
    override val prot: ClientProt,
) : ProxyMessageDecoder<ResumePCountDialogLong> {
    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): ResumePCountDialogLong {
        val value = buffer.g8()
        return ResumePCountDialogLong(
            value,
        )
    }
}
