package net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.misc.user

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.incoming.model.misc.user.FaceSquare
import net.rsprox.protocol.session.Session

internal class FaceSquareDecoder(
    override val prot: ClientProt,
) : ProxyMessageDecoder<FaceSquare> {
    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): FaceSquare {
        val x = buffer.g2Alt2()
        val z = buffer.g2Alt3()
        return FaceSquare(
            x,
            z,
        )
    }
}
