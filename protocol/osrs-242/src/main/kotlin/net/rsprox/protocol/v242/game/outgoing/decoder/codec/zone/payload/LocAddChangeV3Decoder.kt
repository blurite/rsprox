package net.rsprox.protocol.v242.game.outgoing.decoder.codec.zone.payload

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.util.OpFlags
import net.rsprox.protocol.game.outgoing.model.zone.payload.LocAddChangeV3
import net.rsprox.protocol.game.outgoing.model.zone.payload.util.CoordInZone
import net.rsprox.protocol.game.outgoing.model.zone.payload.util.LocProperties
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v242.game.outgoing.decoder.prot.GameServerProt

internal class LocAddChangeV3Decoder : ProxyMessageDecoder<LocAddChangeV3> {
    override val prot: ClientProt = GameServerProt.LOC_ADD_CHANGE_V3

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): LocAddChangeV3 {
        val coordInZone = CoordInZone(buffer.g1Alt2())
        val opFlags = OpFlags(buffer.g1Alt2())
        val opOverrideCount = buffer.g1()
        val opOverrides: Map<Byte, String>? =
            if (opOverrideCount > 0) {
                buildMap {
                    for (i in 0..<opOverrideCount) {
                        val index = buffer.g1()
                        val string = buffer.gjstr()
                        put(index.toByte(), string)
                    }
                }
            } else {
                null
            }
        val id = buffer.g3Alt2()
        val locProperties = LocProperties(buffer.g1Alt3())
        return LocAddChangeV3(
            id,
            coordInZone,
            locProperties,
            opFlags,
            opOverrides,
        )
    }
}
