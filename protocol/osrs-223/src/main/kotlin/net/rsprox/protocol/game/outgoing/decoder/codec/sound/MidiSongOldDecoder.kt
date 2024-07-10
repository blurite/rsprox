package net.rsprox.protocol.game.outgoing.decoder.codec.sound

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.tools.MessageDecodingTools
import net.rsprox.protocol.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.game.outgoing.model.sound.MidiSongOld

public class MidiSongOldDecoder : MessageDecoder<MidiSongOld> {
    override val prot: ClientProt = GameServerProt.MIDI_SONG_OLD

    override fun decode(
        buffer: JagByteBuf,
        tools: MessageDecodingTools,
    ): MidiSongOld {
        val id = buffer.g2Alt2()
        return MidiSongOld(
            id,
        )
    }
}
