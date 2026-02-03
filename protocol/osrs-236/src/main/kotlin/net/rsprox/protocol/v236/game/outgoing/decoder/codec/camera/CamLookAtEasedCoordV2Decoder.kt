package net.rsprox.protocol.v236.game.outgoing.decoder.codec.camera

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.camera.CamLookAtEasedCoordV2
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v236.game.outgoing.decoder.prot.GameServerProt

internal class CamLookAtEasedCoordV2Decoder : ProxyMessageDecoder<CamLookAtEasedCoordV2> {
    override val prot: ClientProt = GameServerProt.CAM_LOOKAT_EASED_COORD_V2

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): CamLookAtEasedCoordV2 {
        val height = buffer.g2Alt1()
        val z = buffer.g2Alt3()
        val x = buffer.g2Alt2()
        val easing = buffer.g1Alt3()
        val cycles = buffer.g2()
        return CamLookAtEasedCoordV2(
            x,
            z,
            height,
            cycles,
            easing,
        )
    }
}
