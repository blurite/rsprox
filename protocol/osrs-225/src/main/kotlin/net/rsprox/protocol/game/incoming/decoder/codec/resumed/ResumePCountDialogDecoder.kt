package net.rsprox.protocol.game.incoming.decoder.codec.resumed

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.incoming.decoder.prot.GameClientProt
import net.rsprox.protocol.game.incoming.model.resumed.ResumePCountDialog
import net.rsprox.protocol.session.Session

@Consistent
public class ResumePCountDialogDecoder : ProxyMessageDecoder<ResumePCountDialog> {
    override val prot: ClientProt = GameClientProt.RESUME_P_COUNTDIALOG

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): ResumePCountDialog {
        val count = buffer.g4()
        return ResumePCountDialog(count)
    }
}
