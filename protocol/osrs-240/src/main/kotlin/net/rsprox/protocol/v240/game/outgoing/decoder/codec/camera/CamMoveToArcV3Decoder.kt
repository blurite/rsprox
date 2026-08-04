package net.rsprox.protocol.v240.game.outgoing.decoder.codec.camera

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.camera.CamMoveToArcV3
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v240.game.outgoing.decoder.prot.GameServerProt

internal class CamMoveToArcV3Decoder : ProxyMessageDecoder<CamMoveToArcV3> {
    override val prot: ClientProt = GameServerProt.CAM_MOVETO_ARC_V3

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): CamMoveToArcV3 {
        val centerZ = buffer.g2Alt2()
        val heightRelative = buffer.g1Alt1() == 1
        val height = buffer.g2sAlt3()
        val easing = buffer.g1Alt2()
        val destinationZ = buffer.g2Alt3()
        val cycles = buffer.g2Alt2()
        val ignoreTerrain = buffer.g1() == 1
        val centerX = buffer.g2Alt3()
        val destinationX = buffer.g2Alt1()
        return CamMoveToArcV3(
            centerX,
            centerZ,
            destinationX,
            destinationZ,
            height,
            cycles,
            ignoreTerrain,
            easing,
            heightRelative,
        )
    }
}
