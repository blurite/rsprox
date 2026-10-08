package net.rsprox.protocol.rs3v950beta.game.incoming.decoder.codec.misc.user

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.incoming.model.misc.user.MoveGameClick
import net.rsprox.protocol.session.Session

internal class MoveGameClickDecoder(
    override val prot: ClientProt,
) : ProxyMessageDecoder<MoveGameClick> {
    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): MoveGameClick {
        val x = buffer.g2Alt3()
        val run = (buffer.g1Alt2() and 1) != 0
        val y = buffer.g2Alt1()
        return MoveGameClick(x, y, run)
    }
}
