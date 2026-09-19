package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.camera

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.camera.CamShake
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class CamShakeDecoder : ProxyMessageDecoder<CamShake> {
    override val prot: ClientProt = GameServerProt.CAM_SHAKE

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): CamShake {
        val axis = buffer.g1Alt2()
        val frequency = buffer.g1Alt1()
        val sineAmplitude = buffer.g1Alt3()
        val randomAmplitude = buffer.g1Alt2()
        val duration = buffer.g2()
        return CamShake(
            axis,
            randomAmplitude,
            sineAmplitude,
            duration,
            frequency,
        )
    }
}
