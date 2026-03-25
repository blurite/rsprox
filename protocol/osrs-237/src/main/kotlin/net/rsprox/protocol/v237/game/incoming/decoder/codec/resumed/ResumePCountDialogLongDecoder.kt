package net.rsprox.protocol.v237.game.incoming.decoder.codec.resumed

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.incoming.model.resumed.ResumePCountDialogLong
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v237.game.incoming.decoder.prot.GameClientProt

@Consistent
public class ResumePCountDialogLongDecoder : ProxyMessageDecoder<ResumePCountDialogLong> {
    override val prot: ClientProt = GameClientProt.RESUME_P_COUNTDIALOG_LONG

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): ResumePCountDialogLong {
        val count = buffer.g8()
        return ResumePCountDialogLong(count)
    }
}
