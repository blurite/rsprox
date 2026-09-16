package net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.dialog

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.incoming.model.dialog.ResumePClanForumQfcDialog
import net.rsprox.protocol.rs3v950.buffer.readNativeString
import net.rsprox.protocol.session.Session

internal class ResumePClanForumQfcDialogDecoder(
    override val prot: ClientProt,
) : ProxyMessageDecoder<ResumePClanForumQfcDialog> {
    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): ResumePClanForumQfcDialog {
        val value = buffer.readNativeString()
        return ResumePClanForumQfcDialog(
            value,
        )
    }
}
