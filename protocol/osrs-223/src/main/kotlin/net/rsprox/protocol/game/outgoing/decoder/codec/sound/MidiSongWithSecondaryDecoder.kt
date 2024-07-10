package net.rsprox.protocol.game.outgoing.decoder.codec.sound

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.tools.MessageDecodingTools
import net.rsprox.protocol.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.game.outgoing.model.sound.MidiSongWithSecondary

public class MidiSongWithSecondaryDecoder : MessageDecoder<MidiSongWithSecondary> {
    override val prot: ClientProt = GameServerProt.MIDI_SONG_WITHSECONDARY

    override fun decode(
        buffer: JagByteBuf,
        tools: MessageDecodingTools,
    ): MidiSongWithSecondary {
        val fadeOutDelay = buffer.g2()
        val secondaryId = buffer.g2Alt2()
        val fadeOutSpeed = buffer.g2Alt3()
        val fadeInSpeed = buffer.g2()
        val fadeInDelay = buffer.g2Alt3()
        val primaryId = buffer.g2Alt2()
        return MidiSongWithSecondary(
            primaryId,
            secondaryId,
            fadeOutDelay,
            fadeOutSpeed,
            fadeInDelay,
            fadeInSpeed,
        )
    }
}
