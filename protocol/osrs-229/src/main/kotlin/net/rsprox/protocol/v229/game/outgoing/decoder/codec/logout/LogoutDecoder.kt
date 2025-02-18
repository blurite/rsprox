package net.rsprox.protocol.v229.game.outgoing.decoder.codec.logout

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.logout.Logout
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v229.game.outgoing.decoder.prot.GameServerProt

@Consistent
internal class LogoutDecoder : ProxyMessageDecoder<Logout> {
    override val prot: ClientProt = GameServerProt.LOGOUT

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): Logout {
        return Logout
    }
}
