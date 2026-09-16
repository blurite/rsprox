package net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.misc.user

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.incoming.model.misc.user.MoveScripted
import net.rsprox.protocol.session.Session

internal class MoveScriptedDecoder(
    override val prot: ClientProt,
) : ProxyMessageDecoder<MoveScripted> {
    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): MoveScripted {
        val z = buffer.g2Alt1()
        val x = buffer.g2Alt1()
        val mode = buffer.g1Alt1()
        return MoveScripted(
            z,
            x,
            mode,
        )
    }
}
