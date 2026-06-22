package net.rsprox.protocol.v239.game.outgoing.decoder.codec.camera

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.camera.CamMoveToArcV2
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v239.game.outgoing.decoder.prot.GameServerProt

internal class CamMoveToArcV2Decoder : ProxyMessageDecoder<CamMoveToArcV2> {
    override val prot: ClientProt = GameServerProt.CAM_MOVETO_ARC_V2

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): CamMoveToArcV2 {
        val ignoreTerrain = buffer.g1() == 1
        val destinationZ = buffer.g2Alt2()
        val cycles = buffer.g2Alt2()
        val height = buffer.g2Alt1()
        val centerX = buffer.g2Alt1()
        val destinationX = buffer.g2Alt1()
        val centerZ = buffer.g2Alt2()
        val easing = buffer.g1Alt3()
        return CamMoveToArcV2(
            centerX,
            centerZ,
            destinationX,
            destinationZ,
            height,
            cycles,
            ignoreTerrain,
            easing,
        )
    }
}
