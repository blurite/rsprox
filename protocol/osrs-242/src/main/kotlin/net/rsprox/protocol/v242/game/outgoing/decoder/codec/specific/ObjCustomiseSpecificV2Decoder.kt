package net.rsprox.protocol.v242.game.outgoing.decoder.codec.specific

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.common.CoordGrid
import net.rsprox.protocol.game.outgoing.model.specific.ObjCustomiseSpecificV2
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v242.game.outgoing.decoder.prot.GameServerProt

internal class ObjCustomiseSpecificV2Decoder : ProxyMessageDecoder<ObjCustomiseSpecificV2> {
    override val prot: ClientProt = GameServerProt.OBJ_CUSTOMISE_SPECIFIC_V2

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): ObjCustomiseSpecificV2 {
        val retextureCount = buffer.g1()
        val retextures =
            List(retextureCount) {
                val index = buffer.g1()
                val value = buffer.g2()
                ObjCustomiseSpecificV2.Replacement(index, value)
            }
        val id = buffer.g2Alt1()
        val recolourCount = buffer.g1Alt1()
        val recolours =
            List(recolourCount) {
                val index = buffer.g1Alt3()
                val value = buffer.g2Alt2()
                ObjCustomiseSpecificV2.Replacement(index, value)
            }
        val hasColour = buffer.g1Alt1() == 1
        val colour = if (hasColour) buffer.g2() else null
        val quantity = buffer.g4Alt3()
        val packedCoord = buffer.g4Alt3()
        val coord = CoordGrid(packedCoord)
        val modelId = buffer.g2Alt3()
        val model = if (modelId == 65535) -1 else modelId
        return ObjCustomiseSpecificV2(id, quantity, model, recolours, retextures, colour, coord)
    }
}
