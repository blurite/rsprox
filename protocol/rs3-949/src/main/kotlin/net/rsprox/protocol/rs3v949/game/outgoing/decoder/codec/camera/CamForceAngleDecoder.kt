package net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.camera

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.rs3.game.outgoing.model.camera.CamForceAngle
import net.rsprox.protocol.session.Session

internal class CamForceAngleDecoder : ProxyMessageDecoder<CamForceAngle> {
    override val prot: ClientProt = GameServerProt.CAM_FORCE_ANGLE

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): CamForceAngle {
        val yaw = buffer.g2()
        val pitch = buffer.g2Alt2()
        return CamForceAngle(
            yaw,
            pitch,
        )
    }
}
