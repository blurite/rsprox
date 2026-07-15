package net.rsprox.protocol.v239.game.outgoing.decoder.codec.sound

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.sound.AmbientSoundStart
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v239.game.outgoing.decoder.prot.GameServerProt

internal class AmbientSoundStartDecoder : ProxyMessageDecoder<AmbientSoundStart> {
    override val prot: ClientProt = GameServerProt.AMBIENTSOUND_START

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): AmbientSoundStart {
        val fade = buffer.g1Alt1() == 1
        val id = buffer.g2()
        return AmbientSoundStart(
            id,
            fade,
        )
    }
}
