package net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.dialog

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.incoming.model.dialog.ResumePauseButton
import net.rsprox.protocol.session.Session

internal class ResumePauseButtonDecoder(
    override val prot: ClientProt,
) : ProxyMessageDecoder<ResumePauseButton> {
    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): ResumePauseButton {
        val combinedId = buffer.g4()
        val sub = buffer.g2Alt2()
        return ResumePauseButton(
            combinedId,
            sub,
        )
    }
}
