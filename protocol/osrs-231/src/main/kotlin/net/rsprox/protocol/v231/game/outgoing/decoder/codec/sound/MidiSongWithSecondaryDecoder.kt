package net.rsprox.protocol.v231.game.outgoing.decoder.codec.sound

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.sound.MidiSongWithSecondary
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v231.game.outgoing.decoder.prot.GameServerProt

internal class MidiSongWithSecondaryDecoder : ProxyMessageDecoder<MidiSongWithSecondary> {
    override val prot: ClientProt = GameServerProt.MIDI_SONG_WITHSECONDARY

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): MidiSongWithSecondary {
        val fadeInDelay = buffer.g2Alt1()
        val primaryId = buffer.g2Alt2()
        val fadeOutDelay = buffer.g2()
        val fadeInSpeed = buffer.g2Alt3()
        val secondaryId = buffer.g2()
        val fadeOutSpeed = buffer.g2Alt2()
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
