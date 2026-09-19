package net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.events

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.incoming.model.events.Cutscene2DFinished
import net.rsprox.protocol.session.Session

internal class Cutscene2DFinishedDecoder(
    override val prot: ClientProt,
) : ProxyMessageDecoder<Cutscene2DFinished> {
    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): Cutscene2DFinished {
        val status = buffer.g1Alt3()
        val id = buffer.g2Alt1()
        return Cutscene2DFinished(
            status,
            id,
        )
    }
}
