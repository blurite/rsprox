package net.rsprox.protocol.v226.game.outgoing.decoder.codec.camera

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.camera.CamMode
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v226.game.outgoing.decoder.prot.GameServerProt

@Consistent
public class CamModeDecoder : ProxyMessageDecoder<CamMode> {
    override val prot: ClientProt = GameServerProt.CAM_MODE

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): CamMode {
        val mode = buffer.g1()
        return CamMode(
            mode,
        )
    }
}
