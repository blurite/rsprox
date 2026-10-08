package net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.camera

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.camera.CamRemoveRoof
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class CamRemoveRoofDecoder : ProxyMessageDecoder<CamRemoveRoof> {
    override val prot: ClientProt = GameServerProt.CAM_REMOVEROOF

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): CamRemoveRoof {
        val coordinate = buffer.g4Alt2()
        return CamRemoveRoof(
            coordinate,
        )
    }
}
