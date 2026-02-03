package net.rsprox.protocol.v236.game.outgoing.decoder.codec.camera

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.camera.CamMoveToCyclesV2
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v236.game.outgoing.decoder.prot.GameServerProt

internal class CamMoveToCyclesV2Decoder : ProxyMessageDecoder<CamMoveToCyclesV2> {
    override val prot: ClientProt = GameServerProt.CAM_MOVETO_CYCLES_V2

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): CamMoveToCyclesV2 {
        val x = buffer.g2Alt1()
        val ignoreTerrain = buffer.g1() == 1
        val easing = buffer.g1()
        val cycles = buffer.g2Alt1()
        val z = buffer.g2Alt3()
        val height = buffer.g2Alt2()
        return CamMoveToCyclesV2(
            x,
            z,
            height,
            cycles,
            ignoreTerrain,
            easing,
        )
    }
}
