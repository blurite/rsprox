package net.rsprox.protocol.v223.game.outgoing.decoder.codec.camera

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.camera.CamReset
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v223.game.outgoing.decoder.prot.GameServerProt

@Consistent
public class CamResetDecoder : ProxyMessageDecoder<CamReset> {
    override val prot: ClientProt = GameServerProt.CAM_RESET

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): CamReset {
        return CamReset
    }
}
