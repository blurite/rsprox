package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.sound

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.sound.MidiSongStop
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class MidiSongStopDecoder : ProxyMessageDecoder<MidiSongStop> {
    override val prot: ClientProt = GameServerProt.MIDI_SONG_STOP

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): MidiSongStop {
        return MidiSongStop
    }
}
