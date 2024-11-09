package net.rsprox.protocol.v226.game.outgoing.decoder.codec.camera

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.camera.CamShake
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v226.game.outgoing.decoder.prot.GameServerProt

@Consistent
internal class CamShakeDecoder : ProxyMessageDecoder<CamShake> {
    override val prot: ClientProt = GameServerProt.CAM_SHAKE

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): CamShake {
        val type = buffer.g1()
        val randomAmount = buffer.g1()
        val sineAmount = buffer.g1()
        val sineFrequency = buffer.g1()
        return CamShake(
            type,
            randomAmount,
            sineAmount,
            sineFrequency,
        )
    }
}
