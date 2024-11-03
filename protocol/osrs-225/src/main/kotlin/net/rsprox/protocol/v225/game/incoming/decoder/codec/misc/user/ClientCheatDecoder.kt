package net.rsprox.protocol.v225.game.incoming.decoder.codec.misc.user

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.incoming.model.misc.user.ClientCheat
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v225.game.incoming.decoder.prot.GameClientProt

@Consistent
internal class ClientCheatDecoder : ProxyMessageDecoder<ClientCheat> {
    override val prot: ClientProt = GameClientProt.CLIENT_CHEAT

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): ClientCheat {
        val command = buffer.gjstr()
        return ClientCheat(command)
    }
}
