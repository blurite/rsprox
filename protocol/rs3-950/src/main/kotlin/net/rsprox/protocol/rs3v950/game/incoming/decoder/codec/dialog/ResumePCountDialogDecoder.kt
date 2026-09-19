package net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.dialog

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.incoming.model.dialog.ResumePCountDialog
import net.rsprox.protocol.session.Session

internal class ResumePCountDialogDecoder(
    override val prot: ClientProt,
) : ProxyMessageDecoder<ResumePCountDialog> {
    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): ResumePCountDialog {
        val value = buffer.g4()
        return ResumePCountDialog(
            value,
        )
    }
}
