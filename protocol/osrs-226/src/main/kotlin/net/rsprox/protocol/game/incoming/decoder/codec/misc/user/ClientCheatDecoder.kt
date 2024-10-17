package net.rsprox.protocol.game.incoming.decoder.codec.misc.user
import net.rsprox.protocol.session.Session

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.game.incoming.model.misc.user.ClientCheat
import net.rsprox.protocol.game.incoming.decoder.prot.GameClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprot.protocol.metadata.Consistent

@Consistent
public class ClientCheatDecoder : ProxyMessageDecoder<ClientCheat> {
    override val prot: ClientProt = GameClientProt.CLIENT_CHEAT

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): ClientCheat {
        val command = buffer.gjstr()
        return ClientCheat(command)
    }
}
