package net.rsprox.protocol.game.outgoing.decoder.codec.sound

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.game.outgoing.model.sound.MidiSwap
import net.rsprox.protocol.session.Session

public class MidiSwapDecoder : ProxyMessageDecoder<MidiSwap> {
    override val prot: ClientProt = GameServerProt.MIDI_SWAP

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): MidiSwap {
        val fadeOutDelay = buffer.g2Alt2()
        val fadeInSpeed = buffer.g2()
        val fadeOutSpeed = buffer.g2Alt2()
        val fadeInDelay = buffer.g2Alt2()
        return MidiSwap(
            fadeOutDelay,
            fadeOutSpeed,
            fadeInDelay,
            fadeInSpeed,
        )
    }
}
