package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.sound

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.sound.MidiSong
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class MidiSongDecoder : ProxyMessageDecoder<MidiSong> {
    override val prot: ClientProt = GameServerProt.MIDI_SONG

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): MidiSong {
        val id = buffer.g4()
        val volume = buffer.g1Alt3()
        return MidiSong(
            id = id,
            volume = volume,
        )
    }
}
