package net.rsprox.protocol.game.incoming.decoder.codec.misc.client
import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.incoming.decoder.prot.GameClientProt
import net.rsprox.protocol.game.incoming.model.misc.client.Idle
import net.rsprox.protocol.session.Session

@Consistent
public class IdleDecoder : ProxyMessageDecoder<Idle> {
    override val prot: ClientProt = GameClientProt.IDLE

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): Idle = Idle
}
