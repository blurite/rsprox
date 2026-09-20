package net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.dialog

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.incoming.model.dialog.ResumePLongDialog
import net.rsprox.protocol.session.Session

internal class ResumePLongDialogDecoder(
    override val prot: ClientProt,
) : ProxyMessageDecoder<ResumePLongDialog> {
    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): ResumePLongDialog {
        val value = buffer.g8()
        return ResumePLongDialog(
            value,
        )
    }
}
