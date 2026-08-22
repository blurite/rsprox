package net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.sound

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.rs3v949.game.outgoing.model.sound.MidiSong
import net.rsprox.protocol.session.Session

internal class MidiSongDecoder : ProxyMessageDecoder<MidiSong> {
    override val prot: ClientProt = GameServerProt.MIDI_SONG

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): MidiSong {
        val idByte0 = buffer.g1()
        val idByte1 = buffer.g1()
        val tailByte = buffer.g1()
        val extraByte = buffer.g1()
        return MidiSong(
            idByte0,
            idByte1,
            tailByte,
            extraByte,
        )
    }
}
