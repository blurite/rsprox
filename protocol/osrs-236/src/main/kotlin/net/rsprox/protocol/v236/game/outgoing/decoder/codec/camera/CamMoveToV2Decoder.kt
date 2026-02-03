package net.rsprox.protocol.v236.game.outgoing.decoder.codec.camera

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.camera.CamMoveToV2
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v236.game.outgoing.decoder.prot.GameServerProt

internal class CamMoveToV2Decoder : ProxyMessageDecoder<CamMoveToV2> {
    override val prot: ClientProt = GameServerProt.CAM_MOVETO_V2

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): CamMoveToV2 {
        val z = buffer.g2Alt3()
        val x = buffer.g2()
        val height = buffer.g2Alt1()
        val rate2 = buffer.g1()
        val rate = buffer.g1Alt1()
        return CamMoveToV2(
            x,
            z,
            height,
            rate,
            rate2,
        )
    }
}
