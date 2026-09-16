package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.sound

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.sound.SynthSound
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class SynthSoundDecoder : ProxyMessageDecoder<SynthSound> {
    override val prot: ClientProt = GameServerProt.SYNTH_SOUND

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): SynthSound {
        val id = buffer.g4()
        val loops = buffer.g1()
        val delay = buffer.g2()
        val volume = buffer.g1()
        val rate = buffer.g2()
        return SynthSound(
            id = id,
            loops = loops,
            delay = delay,
            volume = volume,
            rate = rate,
        )
    }
}
