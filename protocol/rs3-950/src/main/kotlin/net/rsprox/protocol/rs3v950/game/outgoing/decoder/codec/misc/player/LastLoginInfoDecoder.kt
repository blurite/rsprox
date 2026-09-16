package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.misc.player

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.misc.player.LastLoginInfo
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class LastLoginInfoDecoder : ProxyMessageDecoder<LastLoginInfo> {
    override val prot: ClientProt = GameServerProt.LAST_LOGIN_INFO

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): LastLoginInfo {
        val lastLogin = buffer.g4()
        return LastLoginInfo(
            lastLogin,
        )
    }
}
