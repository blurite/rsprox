package net.rsprox.protocol.game.outgoing.decoder.codec.camera

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.game.outgoing.model.camera.CamLookAtEasedAngleRelative
import net.rsprox.protocol.session.Session

@Consistent
public class CamLookAtEasedAngleRelativeDecoder : ProxyMessageDecoder<CamLookAtEasedAngleRelative> {
    override val prot: ClientProt = GameServerProt.CAM_LOOKAT_EASED_ANGLE_RELATIVE

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): CamLookAtEasedAngleRelative {
        val yAngle = buffer.g2()
        val xAngle = buffer.g2()
        val duration = buffer.g2()
        val function = buffer.g1()
        return CamLookAtEasedAngleRelative(
            xAngle,
            yAngle,
            duration,
            function,
        )
    }
}
