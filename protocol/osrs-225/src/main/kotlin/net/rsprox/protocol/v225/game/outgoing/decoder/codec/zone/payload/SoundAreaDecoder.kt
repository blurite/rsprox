package net.rsprox.protocol.v225.game.outgoing.decoder.codec.zone.payload

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.zone.payload.SoundArea
import net.rsprox.protocol.game.outgoing.model.zone.payload.util.CoordInZone
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v225.game.outgoing.decoder.prot.GameServerProt

public class SoundAreaDecoder : ProxyMessageDecoder<SoundArea> {
    override val prot: ClientProt = GameServerProt.SOUND_AREA

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): SoundArea {
        val radius = buffer.g1()
        val id = buffer.g2Alt2()
        val size = buffer.g1Alt3()
        val loops = buffer.g1Alt3()
        val delay = buffer.g1()
        val coordInZone = CoordInZone(buffer.g1Alt1())
        return SoundArea(
            id,
            delay,
            loops,
            radius,
            size,
            coordInZone,
        )
    }
}
