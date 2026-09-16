package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.camera

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.camera.CamMoveTo
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class CamMoveToDecoder : ProxyMessageDecoder<CamMoveTo> {
    override val prot: ClientProt = GameServerProt.CAM_MOVETO

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): CamMoveTo {
        val height = buffer.g2Alt2()
        val localZ = buffer.g1()
        val localX = buffer.g1()
        val accel = buffer.g1Alt1()
        val speed = buffer.g1Alt3()
        return CamMoveTo(
            localX,
            localZ,
            height,
            speed,
            accel,
        )
    }
}
