package net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.dialog

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.incoming.model.dialog.ResumePObjDialog
import net.rsprox.protocol.session.Session

internal class ResumePObjDialogDecoder(
    override val prot: ClientProt,
) : ProxyMessageDecoder<ResumePObjDialog> {
    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): ResumePObjDialog {
        val obj = buffer.g2()
        return ResumePObjDialog(
            obj,
        )
    }
}
