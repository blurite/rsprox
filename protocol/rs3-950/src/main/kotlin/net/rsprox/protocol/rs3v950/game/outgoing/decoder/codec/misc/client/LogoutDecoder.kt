package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.misc.client

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.misc.client.Logout
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class LogoutDecoder : ProxyMessageDecoder<Logout> {
    override val prot: ClientProt = GameServerProt.LOGOUT

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): Logout {
        val reason = buffer.g1()
        return Logout(
            reason,
        )
    }
}
