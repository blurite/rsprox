package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.zone.payload

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.zone.payload.SoundArea
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class SoundAreaDecoder : ProxyMessageDecoder<SoundArea> {
    override val prot: ClientProt = GameServerProt.SOUND_AREA

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): SoundArea {
        val startIndex = buffer.buffer.readerIndex()
        val packedCoord = buffer.g1()
        val xInZone = (packedCoord ushr 4) and 0x7
        val zInZone = packedCoord and 0x7
        val soundId = buffer.g4()
        val byte5 = buffer.g1()
        val rotation = byte5 and 0x7
        val loopCount = buffer.g1()
        val heightOffset = buffer.g1()
        val range = buffer.g2()
        val endIndex = buffer.buffer.readerIndex()
        val rawBytes = ByteArray(endIndex - startIndex)
        buffer.buffer.getBytes(startIndex, rawBytes)
        return SoundArea(soundId, xInZone, zInZone, rotation, loopCount, heightOffset, range, rawBytes)
    }
}
