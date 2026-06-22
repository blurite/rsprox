package net.rsprox.protocol.v239.game.outgoing.decoder.codec.sound

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.sound.AmbienceStop
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v239.game.outgoing.decoder.prot.GameServerProt

internal class AmbienceStopDecoder : ProxyMessageDecoder<AmbienceStop> {
    override val prot: ClientProt = GameServerProt.AMBIENCE_STOP

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): AmbienceStop {
        val fade = buffer.g1Alt3() == 1
        return AmbienceStop(
            fade,
        )
    }
}
