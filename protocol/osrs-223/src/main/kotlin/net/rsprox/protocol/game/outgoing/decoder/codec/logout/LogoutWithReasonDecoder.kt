package net.rsprox.protocol.game.outgoing.decoder.codec.logout

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.game.outgoing.model.logout.LogoutWithReason
import net.rsprox.protocol.session.Session

@Consistent
public class LogoutWithReasonDecoder : ProxyMessageDecoder<LogoutWithReason> {
    override val prot: ClientProt = GameServerProt.LOGOUT_WITHREASON

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): LogoutWithReason {
        val reason = buffer.g1()
        return LogoutWithReason(reason)
    }
}
