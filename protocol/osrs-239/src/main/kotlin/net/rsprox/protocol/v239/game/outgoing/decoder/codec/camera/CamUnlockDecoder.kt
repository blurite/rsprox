package net.rsprox.protocol.v239.game.outgoing.decoder.codec.camera

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.camera.CamUnlock
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v239.game.outgoing.decoder.prot.GameServerProt

internal class CamUnlockDecoder : ProxyMessageDecoder<CamUnlock> {
    override val prot: ClientProt = GameServerProt.CAM_UNLOCK

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): CamUnlock {
        val unlock = buffer.g1Alt1() == 1
        return CamUnlock(
            unlock,
        )
    }
}
