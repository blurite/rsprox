package net.rsprox.protocol.v224.game.incoming.decoder.codec.misc.user

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.incoming.model.misc.user.MoveGameClick
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v224.game.incoming.decoder.prot.GameClientProt

internal class MoveGameClickDecoder : ProxyMessageDecoder<MoveGameClick> {
    override val prot: ClientProt = GameClientProt.MOVE_GAMECLICK

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): MoveGameClick {
        val keyCombination = buffer.g1()
        val z = buffer.g2Alt1()
        val x = buffer.g2()
        return MoveGameClick(
            x,
            z,
            keyCombination,
        )
    }
}
