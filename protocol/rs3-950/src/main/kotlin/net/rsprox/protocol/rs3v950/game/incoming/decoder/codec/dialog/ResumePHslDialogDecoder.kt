package net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.dialog

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.incoming.model.dialog.ResumePHslDialog
import net.rsprox.protocol.session.Session

internal class ResumePHslDialogDecoder(
    override val prot: ClientProt,
) : ProxyMessageDecoder<ResumePHslDialog> {
    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): ResumePHslDialog {
        val hsl = buffer.g2()
        return ResumePHslDialog(
            hsl,
        )
    }
}
