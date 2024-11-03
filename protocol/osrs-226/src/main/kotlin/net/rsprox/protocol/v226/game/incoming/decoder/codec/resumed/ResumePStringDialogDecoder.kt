package net.rsprox.protocol.v226.game.incoming.decoder.codec.resumed
import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.incoming.model.resumed.ResumePStringDialog
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v226.game.incoming.decoder.prot.GameClientProt

@Consistent
internal class ResumePStringDialogDecoder : ProxyMessageDecoder<ResumePStringDialog> {
    override val prot: ClientProt = GameClientProt.RESUME_P_STRINGDIALOG

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): ResumePStringDialog {
        val string = buffer.gjstr()
        return ResumePStringDialog(string)
    }
}
