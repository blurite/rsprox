package net.rsprox.protocol.game.outgoing.decoder.codec.sound

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.game.outgoing.model.sound.MidiSongOld
import net.rsprox.protocol.session.Session

public class MidiSongOldDecoder : ProxyMessageDecoder<MidiSongOld> {
    override val prot: ClientProt = GameServerProt.MIDI_SONG_OLD

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): MidiSongOld {
        val id = buffer.g2Alt3()
        return MidiSongOld(
            id,
        )
    }
}
