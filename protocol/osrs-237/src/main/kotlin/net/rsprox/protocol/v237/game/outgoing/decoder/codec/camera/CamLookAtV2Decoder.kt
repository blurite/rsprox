package net.rsprox.protocol.v237.game.outgoing.decoder.codec.camera

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.camera.CamLookAtV2
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v237.game.outgoing.decoder.prot.GameServerProt

internal class CamLookAtV2Decoder : ProxyMessageDecoder<CamLookAtV2> {
    override val prot: ClientProt = GameServerProt.CAM_LOOKAT_V2

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): CamLookAtV2 {
        val x = buffer.g2Alt2()
        val rate = buffer.g1Alt3()
        val height = buffer.g2Alt3()
        val rate2 = buffer.g1()
        val z = buffer.g2()
        return CamLookAtV2(
            x,
            z,
            height,
            rate,
            rate2,
        )
    }
}
