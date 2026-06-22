package net.rsprox.protocol.v239.game.outgoing.decoder.codec.camera

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.camera.CamSkybox
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v239.game.outgoing.decoder.prot.GameServerProt

internal class CamSkyboxDecoder : ProxyMessageDecoder<CamSkybox> {
    override val prot: ClientProt = GameServerProt.CAM_SKYBOX

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): CamSkybox {
        val model = buffer.g4Alt3()
        return CamSkybox(
            model,
        )
    }
}
