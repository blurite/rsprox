package net.rsprox.protocol.v223.game.outgoing.decoder.codec.zone.payload

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.util.OpFlags
import net.rsprox.protocol.game.outgoing.model.zone.payload.LocAddChange
import net.rsprox.protocol.game.outgoing.model.zone.payload.util.CoordInZone
import net.rsprox.protocol.game.outgoing.model.zone.payload.util.LocProperties
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v223.game.outgoing.decoder.prot.GameServerProt

public class LocAddChangeDecoder : ProxyMessageDecoder<LocAddChange> {
    override val prot: ClientProt = GameServerProt.LOC_ADD_CHANGE

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): LocAddChange {
        val locProperties = LocProperties(buffer.g1Alt2())
        val opFlags = OpFlags(buffer.g1Alt3())
        val coordInZone = CoordInZone(buffer.g1Alt3())
        val id = buffer.g2Alt2()
        return LocAddChange(
            id,
            coordInZone,
            locProperties,
            opFlags,
        )
    }
}
