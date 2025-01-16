package net.rsprox.protocol.v228.game.incoming.decoder.codec.resumed

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.incoming.model.resumed.ResumePNameDialog
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v228.game.incoming.decoder.prot.GameClientProt

@Consistent
public class ResumePNameDialogDecoder : ProxyMessageDecoder<ResumePNameDialog> {
    override val prot: ClientProt = GameClientProt.RESUME_P_NAMEDIALOG

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): ResumePNameDialog {
        val name = buffer.gjstr()
        return ResumePNameDialog(name)
    }
}
