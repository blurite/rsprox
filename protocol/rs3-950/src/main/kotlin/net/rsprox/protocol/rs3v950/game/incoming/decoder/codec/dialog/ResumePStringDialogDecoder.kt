package net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.dialog

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.incoming.model.dialog.ResumePStringDialog
import net.rsprox.protocol.rs3v950.buffer.readNativeString
import net.rsprox.protocol.session.Session

internal class ResumePStringDialogDecoder(
    override val prot: ClientProt,
) : ProxyMessageDecoder<ResumePStringDialog> {
    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): ResumePStringDialog {
        val value = buffer.readNativeString()
        return ResumePStringDialog(
            value,
        )
    }
}
