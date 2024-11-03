package net.rsprox.protocol.v226.game.outgoing.decoder.codec.sound

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.sound.MidiSongV1
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v226.game.outgoing.decoder.prot.GameServerProt

public class MidiSongV1Decoder : ProxyMessageDecoder<MidiSongV1> {
    override val prot: ClientProt = GameServerProt.MIDI_SONG_V1

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): MidiSongV1 {
        val id = buffer.g2Alt1()
        return MidiSongV1(
            id,
        )
    }
}
