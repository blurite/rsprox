package net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.camera

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.rs3v949.game.outgoing.model.camera.CamShake
import net.rsprox.protocol.session.Session

internal class CamShakeDecoder : ProxyMessageDecoder<CamShake> {
    override val prot: ClientProt = GameServerProt.CAM_SHAKE

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): CamShake {
        val shakeMode = buffer.g1Alt2()
        val param0 = buffer.g2Alt2()
        val param1 = buffer.g1()
        val param2 = buffer.g1()
        val param3 = buffer.g1()
        return CamShake(
            shakeMode,
            param0,
            param1,
            param2,
            param3,
        )
    }
}
