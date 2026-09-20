package net.rsprox.protocol.v238.game.incoming.decoder.codec.resumed

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.incoming.model.resumed.ResumePLongDialog
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v238.game.incoming.decoder.prot.GameClientProt

@Consistent
public class ResumePLongDialogDecoder : ProxyMessageDecoder<ResumePLongDialog> {
    override val prot: ClientProt = GameClientProt.RESUME_P_LONGDIALOG

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): ResumePLongDialog {
        val count = buffer.g8()
        return ResumePLongDialog(count)
    }
}
