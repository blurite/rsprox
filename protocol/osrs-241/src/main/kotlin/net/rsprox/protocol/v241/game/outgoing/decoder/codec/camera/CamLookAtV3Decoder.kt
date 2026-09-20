package net.rsprox.protocol.v241.game.outgoing.decoder.codec.camera

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.camera.CamLookAtV3
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v241.game.outgoing.decoder.prot.GameServerProt

internal class CamLookAtV3Decoder : ProxyMessageDecoder<CamLookAtV3> {
    override val prot: ClientProt = GameServerProt.CAM_LOOKAT_V3

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): CamLookAtV3 {
        val height = buffer.g2s()
        val x = buffer.g2Alt3()
        val rate2 = buffer.g1Alt1()
        val rate = buffer.g1()
        val heightRelative = buffer.g1() == 1
        val z = buffer.g2()
        return CamLookAtV3(
            x,
            z,
            height,
            rate,
            rate2,
            heightRelative,
        )
    }
}
