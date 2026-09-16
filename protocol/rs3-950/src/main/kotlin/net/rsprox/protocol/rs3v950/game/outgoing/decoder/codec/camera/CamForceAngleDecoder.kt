package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.camera

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.camera.CamForceAngle
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class CamForceAngleDecoder : ProxyMessageDecoder<CamForceAngle> {
    override val prot: ClientProt = GameServerProt.CAM_FORCEANGLE

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): CamForceAngle {
        val angle1 = buffer.g2Alt1()
        val angle0 = buffer.g2Alt1()
        return CamForceAngle(
            angle0,
            angle1,
        )
    }
}
