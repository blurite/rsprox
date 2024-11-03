package net.rsprox.protocol.v226.game.incoming.decoder.codec.misc.user
import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.v226.game.incoming.decoder.prot.GameClientProt
import net.rsprox.protocol.game.incoming.model.misc.user.MoveGameClick
import net.rsprox.protocol.session.Session

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
