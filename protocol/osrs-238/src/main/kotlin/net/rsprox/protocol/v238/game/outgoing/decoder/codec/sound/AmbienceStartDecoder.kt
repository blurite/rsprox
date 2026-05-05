package net.rsprox.protocol.v238.game.outgoing.decoder.codec.sound

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.sound.AmbienceStart
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v238.game.outgoing.decoder.prot.GameServerProt

internal class AmbienceStartDecoder : ProxyMessageDecoder<AmbienceStart> {
    override val prot: ClientProt = GameServerProt.AMBIENCE_START

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): AmbienceStart {
        val fade = buffer.g1Alt1() == 1
        val id = buffer.g2()
        return AmbienceStart(
            id,
            fade,
        )
    }
}
