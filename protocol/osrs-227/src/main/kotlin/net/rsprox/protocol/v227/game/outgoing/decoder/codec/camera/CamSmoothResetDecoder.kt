package net.rsprox.protocol.v227.game.outgoing.decoder.codec.camera

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.camera.CamSmoothReset
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v227.game.outgoing.decoder.prot.GameServerProt

@Consistent
internal class CamSmoothResetDecoder : ProxyMessageDecoder<CamSmoothReset> {
    override val prot: ClientProt = GameServerProt.CAM_SMOOTHRESET

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): CamSmoothReset {
        val cameraMoveConstantSpeed = buffer.g1()
        val cameraMoveProportionalSpeed = buffer.g1()
        val cameraLookConstantSpeed = buffer.g1()
        val cameraLookProportionalSpeed = buffer.g1()
        return CamSmoothReset(
            cameraMoveConstantSpeed,
            cameraMoveProportionalSpeed,
            cameraLookConstantSpeed,
            cameraLookProportionalSpeed,
        )
    }
}
