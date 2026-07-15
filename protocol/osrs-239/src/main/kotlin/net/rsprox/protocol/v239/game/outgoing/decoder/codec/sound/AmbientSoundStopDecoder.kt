package net.rsprox.protocol.v239.game.outgoing.decoder.codec.sound

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.sound.AmbientSoundStop
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v239.game.outgoing.decoder.prot.GameServerProt

internal class AmbientSoundStopDecoder : ProxyMessageDecoder<AmbientSoundStop> {
    override val prot: ClientProt = GameServerProt.AMBIENTSOUND_STOP

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): AmbientSoundStop {
        val fade = buffer.g1() == 1
        return AmbientSoundStop(
            fade,
        )
    }
}
