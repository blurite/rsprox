package net.rsprox.protocol.game.outgoing.decoder.codec.sound

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.tools.MessageDecodingTools
import net.rsprox.protocol.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.game.outgoing.model.sound.MidiSwap

public class MidiSwapDecoder : MessageDecoder<MidiSwap> {
    override val prot: ClientProt = GameServerProt.MIDI_SWAP

    override fun decode(
        buffer: JagByteBuf,
        tools: MessageDecodingTools,
    ): MidiSwap {
        val fadeInDelay = buffer.g2()
        val fadeInSpeed = buffer.g2Alt2()
        val fadeOutSpeed = buffer.g2()
        val fadeOutDelay = buffer.g2()
        return MidiSwap(
            fadeOutDelay,
            fadeOutSpeed,
            fadeInDelay,
            fadeInSpeed,
        )
    }
}
