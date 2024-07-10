package net.rsprox.protocol.game.outgoing.decoder.codec.sound

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.tools.MessageDecodingTools
import net.rsprox.protocol.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.game.outgoing.model.sound.MidiSongStop

public class MidiSongStopDecoder : MessageDecoder<MidiSongStop> {
    override val prot: ClientProt = GameServerProt.MIDI_SONG_STOP

    override fun decode(
        buffer: JagByteBuf,
        tools: MessageDecodingTools,
    ): MidiSongStop {
        val fadeOutSpeed = buffer.g2Alt3()
        val fadeOutDelay = buffer.g2Alt1()
        return MidiSongStop(
            fadeOutDelay,
            fadeOutSpeed,
        )
    }
}
