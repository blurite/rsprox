package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.interfaces.IfSetHttpImage
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class IfSetHttpImageDecoder : ProxyMessageDecoder<IfSetHttpImage> {
    override val prot: ClientProt = GameServerProt.IF_SET_HTTP_IMAGE

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): IfSetHttpImage {
        val componentHash = buffer.g4Alt1().toLong() and 0xFFFF_FFFFL
        val imageId = buffer.g4Alt3()
        return IfSetHttpImage(
            componentHash,
            imageId,
        )
    }
}
