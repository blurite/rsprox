package net.rsprox.protocol.v235.game.outgoing.decoder.codec.zone.payload

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.util.OpFlags
import net.rsprox.protocol.game.outgoing.model.zone.payload.LocAddChangeV2
import net.rsprox.protocol.game.outgoing.model.zone.payload.util.CoordInZone
import net.rsprox.protocol.game.outgoing.model.zone.payload.util.LocProperties
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v235.game.outgoing.decoder.prot.GameServerProt

internal class LocAddChangeV2Decoder : ProxyMessageDecoder<LocAddChangeV2> {
    override val prot: ClientProt = GameServerProt.LOC_ADD_CHANGE_V2

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): LocAddChangeV2 {
        val opOverrideCount = buffer.g1Alt2()
        val opOverrides: Map<Byte, String>? =
            if (opOverrideCount > 0) {
                buildMap {
                    for (i in 0..<opOverrideCount) {
                        val index = buffer.g1Alt3()
                        val string = buffer.gjstr()
                        put(index.toByte(), string)
                    }
                }
            } else {
                null
            }
        val locProperties = LocProperties(buffer.g1Alt2())
        val opFlags = OpFlags(buffer.g1Alt1())
        val id = buffer.g2Alt2()
        val coordInZone = CoordInZone(buffer.g1Alt1())
        return LocAddChangeV2(
            id,
            coordInZone,
            locProperties,
            opFlags,
            opOverrides,
        )
    }
}
