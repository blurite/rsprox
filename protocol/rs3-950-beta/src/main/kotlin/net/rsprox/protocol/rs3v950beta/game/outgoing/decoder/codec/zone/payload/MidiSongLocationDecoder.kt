package net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.zone.payload

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.zone.payload.MidiSongLocation
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class MidiSongLocationDecoder : ProxyMessageDecoder<MidiSongLocation> {
    override val prot: ClientProt = GameServerProt.MIDI_SONG_LOCATION

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): MidiSongLocation {
        val volume = buffer.g1Alt2()
        val id = buffer.g4Alt3()
        val range = buffer.g1Alt2()
        val coordinate = buffer.g4Alt3()
        val radius = buffer.g1Alt1()
        return MidiSongLocation(
            volume = volume,
            id = id,
            range = range,
            coordinate = coordinate,
            radius = radius,
        )
    }
}
