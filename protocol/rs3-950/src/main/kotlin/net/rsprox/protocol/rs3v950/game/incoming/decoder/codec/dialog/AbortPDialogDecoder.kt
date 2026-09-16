package net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.dialog

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.incoming.model.dialog.AbortPDialog
import net.rsprox.protocol.session.Session

internal class AbortPDialogDecoder(
    override val prot: ClientProt,
) : ProxyMessageDecoder<AbortPDialog> {
    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): AbortPDialog {
        return AbortPDialog
    }
}
