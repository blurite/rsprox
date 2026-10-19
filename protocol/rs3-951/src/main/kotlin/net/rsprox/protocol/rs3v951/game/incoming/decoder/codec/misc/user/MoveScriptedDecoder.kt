package net.rsprox.protocol.rs3v951.game.incoming.decoder.codec.misc.user

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
        val x = buffer.g2Alt3()
        val z = buffer.g2()
        val mode = buffer.g1Alt3()
        return MoveScripted(
            z,
            x,
            mode,
        )
    }
}
