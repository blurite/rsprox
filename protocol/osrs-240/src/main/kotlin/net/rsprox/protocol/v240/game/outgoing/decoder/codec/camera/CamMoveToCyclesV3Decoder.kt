package net.rsprox.protocol.v240.game.outgoing.decoder.codec.camera

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.camera.CamMoveToCyclesV3
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v240.game.outgoing.decoder.prot.GameServerProt

internal class CamMoveToCyclesV3Decoder : ProxyMessageDecoder<CamMoveToCyclesV3> {
    override val prot: ClientProt = GameServerProt.CAM_MOVETO_CYCLES_V3

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): CamMoveToCyclesV3 {
        val ignoreTerrain = buffer.g1Alt2() == 1
        val easing = buffer.g1Alt1()
        val heightRelative = buffer.g1() == 1
        val height = buffer.g2sAlt3()
        val z = buffer.g2Alt2()
        val cycles = buffer.g2Alt1()
        val x = buffer.g2Alt3()
        return CamMoveToCyclesV3(
            x,
            z,
            height,
            cycles,
            ignoreTerrain,
            easing,
            heightRelative,
        )
    }
}
