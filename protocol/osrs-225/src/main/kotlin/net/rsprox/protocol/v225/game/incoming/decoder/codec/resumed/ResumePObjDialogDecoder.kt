package net.rsprox.protocol.v225.game.incoming.decoder.codec.resumed

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.incoming.model.resumed.ResumePObjDialog
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v225.game.incoming.decoder.prot.GameClientProt

@Consistent
internal class ResumePObjDialogDecoder : ProxyMessageDecoder<ResumePObjDialog> {
    override val prot: ClientProt = GameClientProt.RESUME_P_OBJDIALOG

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): ResumePObjDialog {
        val obj = buffer.g2()
        return ResumePObjDialog(obj)
    }
}
