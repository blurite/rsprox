package net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.dialog

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.incoming.model.dialog.ResumePNameDialog
import net.rsprox.protocol.rs3v950.buffer.readNativeString
import net.rsprox.protocol.session.Session

internal class ResumePNameDialogDecoder(
    override val prot: ClientProt,
) : ProxyMessageDecoder<ResumePNameDialog> {
    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): ResumePNameDialog {
        val value = buffer.readNativeString()
        return ResumePNameDialog(
            value,
        )
    }
}
