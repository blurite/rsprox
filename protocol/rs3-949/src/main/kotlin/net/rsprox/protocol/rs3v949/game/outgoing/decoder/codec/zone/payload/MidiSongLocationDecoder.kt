package net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.zone.payload

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.zone.payload.MidiSongLocation
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class MidiSongLocationDecoder : ProxyMessageDecoder<MidiSongLocation> {
    override val prot: ClientProt = GameServerProt.MIDI_SONG_LOCATION

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): MidiSongLocation {
        val maxDistance = buffer.g1Alt2()
        val minDistance = buffer.g1Alt2()

        val id = buffer.g4Alt2()

        val coord = buffer.g4Alt2()
        val xInZone = (coord ushr 14) and 0x7
        val zInZone = coord and 0x7

        val volume = buffer.g1Alt1()

        return MidiSongLocation(
            id = id,
            xInZone = xInZone,
            zInZone = zInZone,
            maxDistance = maxDistance,
            minDistance = minDistance,
            volume = volume,
        )
    }
}
