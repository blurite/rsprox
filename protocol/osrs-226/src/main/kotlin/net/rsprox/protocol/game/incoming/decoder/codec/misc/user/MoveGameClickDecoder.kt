package net.rsprox.protocol.game.incoming.decoder.codec.misc.user
import net.rsprox.protocol.session.Session

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.game.incoming.model.misc.user.MoveGameClick
import net.rsprox.protocol.game.incoming.decoder.prot.GameClientProt
import net.rsprox.protocol.ProxyMessageDecoder

public class MoveGameClickDecoder : ProxyMessageDecoder<MoveGameClick> {
    override val prot: ClientProt = GameClientProt.MOVE_GAMECLICK

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): MoveGameClick {
        val keyCombination = buffer.g1Alt1()
        val x = buffer.g2Alt3()
        val z = buffer.g2Alt2()
        return MoveGameClick(
            x,
            z,
            keyCombination,
        )
    }
}
