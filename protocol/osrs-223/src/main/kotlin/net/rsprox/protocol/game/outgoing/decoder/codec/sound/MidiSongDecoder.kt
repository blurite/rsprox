package net.rsprox.protocol.game.outgoing.decoder.codec.sound

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.tools.MessageDecodingTools
import net.rsprox.protocol.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.game.outgoing.model.sound.MidiSong

public class MidiSongDecoder : MessageDecoder<MidiSong> {
    override val prot: ClientProt = GameServerProt.MIDI_SONG

    override fun decode(
        buffer: JagByteBuf,
        tools: MessageDecodingTools,
    ): MidiSong {
        val id = buffer.g2Alt1()
        val fadeInDelay = buffer.g2()
        val fadeOutDelay = buffer.g2Alt1()
        val fadeOutSpeed = buffer.g2Alt3()
        val fadeInSpeed = buffer.g2Alt3()
        return MidiSong(
            id,
            fadeOutDelay,
            fadeOutSpeed,
            fadeInDelay,
            fadeInSpeed,
        )
    }
}
