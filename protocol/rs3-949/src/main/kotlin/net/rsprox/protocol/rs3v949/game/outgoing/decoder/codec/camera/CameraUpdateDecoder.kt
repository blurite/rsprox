package net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.camera

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.rs3v949.game.outgoing.model.camera.CameraUpdate
import net.rsprox.protocol.session.Session

internal class CameraUpdateDecoder : ProxyMessageDecoder<CameraUpdate> {
    override val prot: ClientProt = GameServerProt.CAMERA_UPDATE

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): CameraUpdate {
        val headerFlags = buffer.g1()
        val hasBitmask = (headerFlags and 0x80) != 0
        val bitmask = if (hasBitmask && buffer.isReadable) buffer.g2() else 0
        return CameraUpdate(
            headerFlags,
            bitmask,
        )
    }
}
