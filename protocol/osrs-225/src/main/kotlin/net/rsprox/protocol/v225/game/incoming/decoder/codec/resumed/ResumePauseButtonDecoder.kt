package net.rsprox.protocol.v225.game.incoming.decoder.codec.resumed

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.util.gCombinedIdAlt3
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.incoming.model.resumed.ResumePauseButton
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v225.game.incoming.decoder.prot.GameClientProt

internal class ResumePauseButtonDecoder : ProxyMessageDecoder<ResumePauseButton> {
    override val prot: ClientProt = GameClientProt.RESUME_PAUSEBUTTON

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): ResumePauseButton {
        val sub = buffer.g2Alt3()
        val combinedId = buffer.gCombinedIdAlt3()
        return ResumePauseButton(
            combinedId,
            sub,
        )
    }
}
