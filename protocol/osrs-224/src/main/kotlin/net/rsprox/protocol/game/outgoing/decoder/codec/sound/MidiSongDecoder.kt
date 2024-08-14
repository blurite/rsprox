package net.rsprox.protocol.game.outgoing.decoder.codec.sound

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.game.outgoing.model.sound.MidiSong
import net.rsprox.protocol.session.Session

public class MidiSongDecoder : ProxyMessageDecoder<MidiSong> {
    override val prot: ClientProt = GameServerProt.MIDI_SONG

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): MidiSong {
        val fadeOutDelay = buffer.g2Alt2()
        val fadeOutSpeed = buffer.g2()
        val fadeInSpeed = buffer.g2Alt1()
        val fadeInDelay = buffer.g2Alt2()
        val id = buffer.g2Alt1()
        return MidiSong(
            id,
            fadeOutDelay,
            fadeOutSpeed,
            fadeInDelay,
            fadeInSpeed,
        )
    }
}
