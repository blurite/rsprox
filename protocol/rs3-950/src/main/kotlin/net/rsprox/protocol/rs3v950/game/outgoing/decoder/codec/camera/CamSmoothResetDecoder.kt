package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.camera

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.camera.CamSmoothReset
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

@Consistent
internal class CamSmoothResetDecoder : ProxyMessageDecoder<CamSmoothReset> {
    override val prot: ClientProt = GameServerProt.CAM_SMOOTHRESET

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): CamSmoothReset {
        return CamSmoothReset
    }
}
