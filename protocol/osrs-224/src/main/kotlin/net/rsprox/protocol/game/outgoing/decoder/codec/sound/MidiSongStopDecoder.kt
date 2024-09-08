package net.rsprox.protocol.game.outgoing.decoder.codec.sound

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.game.outgoing.model.sound.MidiSongStop
import net.rsprox.protocol.session.Session

public class MidiSongStopDecoder : ProxyMessageDecoder<MidiSongStop> {
    override val prot: ClientProt = GameServerProt.MIDI_SONG_STOP

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): MidiSongStop {
        val fadeOutSpeed = buffer.g2()
        val fadeOutDelay = buffer.g2Alt2()
        return MidiSongStop(
            fadeOutDelay,
            fadeOutSpeed,
        )
    }
}
