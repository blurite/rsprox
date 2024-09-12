package net.rsprox.protocol.game.outgoing.decoder.codec.sound

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.game.outgoing.model.sound.MidiSongWithSecondary
import net.rsprox.protocol.session.Session

public class MidiSongWithSecondaryDecoder : ProxyMessageDecoder<MidiSongWithSecondary> {
    override val prot: ClientProt = GameServerProt.MIDI_SONG_WITHSECONDARY

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): MidiSongWithSecondary {
        val primaryId = buffer.g2Alt3()
        val fadeInDelay = buffer.g2Alt2()
        val fadeInSpeed = buffer.g2Alt1()
        val fadeOutSpeed = buffer.g2()
        val fadeOutDelay = buffer.g2()
        val secondaryId = buffer.g2Alt1()
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
