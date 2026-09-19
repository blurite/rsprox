package net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.misc.user

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.incoming.model.misc.user.LocSelectSubmit
import net.rsprox.protocol.session.Session

internal class LocSelectSubmitDecoder(
    override val prot: ClientProt,
) : ProxyMessageDecoder<LocSelectSubmit> {
    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): LocSelectSubmit {
        val cursorStyle = buffer.g1Alt1()
        val packedCoordinate = buffer.g4Alt3()
        return LocSelectSubmit(
            cursorStyle,
            packedCoordinate,
        )
    }
}
