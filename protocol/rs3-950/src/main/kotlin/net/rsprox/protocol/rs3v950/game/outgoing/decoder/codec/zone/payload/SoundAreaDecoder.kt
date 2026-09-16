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
        val coordinate = buffer.g1()
        val id = buffer.g4()
        val loopsAndRange = buffer.g1()
        val delay = buffer.g1()
        val volume = buffer.g1()
        val rate = buffer.g2()
        return SoundArea(
            coordinate = coordinate,
            id = id,
            loopsAndRange = loopsAndRange,
            delay = delay,
            volume = volume,
            rate = rate,
        )
    }
}
