package net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.zone.payload

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.rs3v949.game.outgoing.model.zone.payload.MidiSongLocation
import net.rsprox.protocol.session.Session

internal class MidiSongLocationDecoder : ProxyMessageDecoder<MidiSongLocation> {
    override val prot: ClientProt = GameServerProt.MIDI_SONG_LOCATION

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): MidiSongLocation {
        val startIndex = buffer.buffer.readerIndex()
        val packedCoord = buffer.g1()
        val xInZone = (packedCoord ushr 4) and 0x7
        val zInZone = packedCoord and 0x7

        val id = buffer.g2s()
        val heightAdjust = buffer.g2s()
        val flagsValue = buffer.g2()
        val rotationByte = buffer.g1()
        buffer.skipRead(3)

        val endIndex = buffer.buffer.readerIndex()
        val rawBytes = ByteArray(endIndex - startIndex)
        buffer.buffer.getBytes(startIndex, rawBytes)
        return MidiSongLocation(id, xInZone, zInZone, heightAdjust, flagsValue, rotationByte, rawBytes)
    }
}
