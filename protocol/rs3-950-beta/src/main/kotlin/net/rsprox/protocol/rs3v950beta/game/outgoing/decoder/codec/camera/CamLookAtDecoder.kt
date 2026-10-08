package net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.camera

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.camera.CamLookAt
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class CamLookAtDecoder : ProxyMessageDecoder<CamLookAt> {
    override val prot: ClientProt = GameServerProt.CAM_LOOKAT

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): CamLookAt {
        val height = buffer.g2()
        val localZ = buffer.g1Alt3()
        val localX = buffer.g1()
        val speed = buffer.g1()
        val accel = buffer.g1Alt1()
        return CamLookAt(
            localX,
            localZ,
            height,
            speed,
            accel,
        )
    }
}
