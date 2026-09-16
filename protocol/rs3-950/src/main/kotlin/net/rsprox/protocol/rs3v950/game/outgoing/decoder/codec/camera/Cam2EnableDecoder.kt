package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.camera

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.camera.Cam2Enable
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class Cam2EnableDecoder : ProxyMessageDecoder<Cam2Enable> {
    override val prot: ClientProt = GameServerProt.CAM2_ENABLE

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): Cam2Enable {
        val enabled = buffer.g1() == 1
        return Cam2Enable(
            enabled,
        )
    }
}
