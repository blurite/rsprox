package net.rsprox.protocol.v228.game.outgoing.decoder.codec.sound

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.sound.MidiSongV2
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v228.game.outgoing.decoder.prot.GameServerProt

internal class MidiSongV2Decoder : ProxyMessageDecoder<MidiSongV2> {
    override val prot: ClientProt = GameServerProt.MIDI_SONG_V2

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): MidiSongV2 {
        val fadeOutSpeed = buffer.g2Alt1()
        val fadeOutDelay = buffer.g2Alt2()
        val id = buffer.g2Alt1()
        val fadeInSpeed = buffer.g2Alt1()
        val fadeInDelay = buffer.g2Alt1()
        return MidiSongV2(
            id,
            fadeOutDelay,
            fadeOutSpeed,
            fadeInDelay,
            fadeInSpeed,
        )
    }
}
