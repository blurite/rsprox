package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.zone.payload

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.zone.payload.MidiSongLocation
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class MidiSongLocationDecoder : ProxyMessageDecoder<MidiSongLocation> {
    override val prot: ClientProt = GameServerProt.MIDI_SONG_LOCATION

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): MidiSongLocation {
        val id = buffer.g4Alt1()
        val radius = buffer.g1Alt2()
        val coordinate = buffer.g4Alt2()
        val volume = buffer.g1Alt3()
        val range = buffer.g1Alt2()

        return MidiSongLocation(
            id = id,
            radius = radius,
            coordinate = coordinate,
            volume = volume,
            range = range,
        )
    }
}
