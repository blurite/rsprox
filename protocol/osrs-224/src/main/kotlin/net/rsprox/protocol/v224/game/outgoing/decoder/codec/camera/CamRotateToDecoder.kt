package net.rsprox.protocol.v224.game.outgoing.decoder.codec.camera

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.camera.CamRotateTo
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v224.game.outgoing.decoder.prot.GameServerProt

@Consistent
internal class CamRotateToDecoder : ProxyMessageDecoder<CamRotateTo> {
    override val prot: ClientProt = GameServerProt.CAM_ROTATETO

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): CamRotateTo {
        val yAngle = buffer.g2()
        val xAngle = buffer.g2()
        val duration = buffer.g2()
        val function = buffer.g1()
        return CamRotateTo(
            xAngle,
            yAngle,
            duration,
            function,
        )
    }
}
