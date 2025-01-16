package net.rsprox.protocol.v225.game.outgoing.decoder.codec.zone.payload

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.util.OpFlags
import net.rsprox.protocol.game.outgoing.model.zone.payload.LocAddChangeV1
import net.rsprox.protocol.game.outgoing.model.zone.payload.util.CoordInZone
import net.rsprox.protocol.game.outgoing.model.zone.payload.util.LocProperties
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v225.game.outgoing.decoder.prot.GameServerProt

internal class LocAddChangeDecoder : ProxyMessageDecoder<LocAddChangeV1> {
    override val prot: ClientProt = GameServerProt.LOC_ADD_CHANGE_V1

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): LocAddChangeV1 {
        val id = buffer.g2Alt2()
        val opFlags = OpFlags(buffer.g1Alt2())
        val locProperties = LocProperties(buffer.g1Alt2())
        val coordInZone = CoordInZone(buffer.g1Alt2())
        return LocAddChangeV1(
            id,
            coordInZone,
            locProperties,
            opFlags,
        )
    }
}
