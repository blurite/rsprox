package net.rsprox.protocol.v224.game.outgoing.decoder.codec.sound

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.v224.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.game.outgoing.model.sound.SynthSound
import net.rsprox.protocol.session.Session

@Consistent
public class SynthSoundDecoder : ProxyMessageDecoder<SynthSound> {
    override val prot: ClientProt = GameServerProt.SYNTH_SOUND

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): SynthSound {
        val id = buffer.g2()
        val loops = buffer.g1()
        val delay = buffer.g2()
        return SynthSound(
            id,
            loops,
            delay,
        )
    }
}
