package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.misc.client

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.misc.client.LogoutFull
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class LogoutFullDecoder : ProxyMessageDecoder<LogoutFull> {
    override val prot: ClientProt = GameServerProt.LOGOUT_FULL

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): LogoutFull {
        val reason = buffer.g1()
        return LogoutFull(
            reason,
        )
    }
}
