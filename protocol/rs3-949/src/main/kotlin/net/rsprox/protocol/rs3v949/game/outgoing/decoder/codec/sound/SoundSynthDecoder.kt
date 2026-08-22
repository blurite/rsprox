package net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.sound

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.rs3v949.game.outgoing.model.sound.SoundSynth
import net.rsprox.protocol.session.Session

internal class SoundSynthDecoder : ProxyMessageDecoder<SoundSynth> {
    override val prot: ClientProt = GameServerProt.SOUND_SYNTH

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): SoundSynth {
        val soundId = buffer.g4()
        val loops = buffer.g1()
        val delay = buffer.g2()
        val volume = buffer.g1()
        val pitch = buffer.g2()
        return SoundSynth(
            soundId,
            loops,
            delay,
            volume,
            pitch,
        )
    }
}
