package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.zone.payload

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.zone.payload.SoundAreaV2
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameZoneProt
import net.rsprox.protocol.session.Session

internal class SoundAreaV2Decoder : ProxyMessageDecoder<SoundAreaV2> {
    override val prot: ClientProt = GameZoneProt.SOUND_AREA_V2

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): SoundAreaV2 {
        val coordinate = buffer.g1()
        val id = buffer.g4()
        val loopsAndRange = buffer.g1()
        val delay = buffer.g1()
        val volume = buffer.g1()
        val rate = buffer.g2()
        val extendedAudioMode = buffer.g1()
        return SoundAreaV2(
            coordinate = coordinate,
            id = id,
            loopsAndRange = loopsAndRange,
            delay = delay,
            volume = volume,
            rate = rate,
            extendedAudioMode = extendedAudioMode,
        )
    }
}
