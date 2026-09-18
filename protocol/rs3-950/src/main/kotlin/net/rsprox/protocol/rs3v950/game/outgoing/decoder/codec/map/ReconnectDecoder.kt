package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.map

import net.rsprot.buffer.JagByteBuf
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.map.Reconnect
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.info.playerinfo.initializePlayerInfo
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.info.playerinfo.readPlayerInfoInit
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

/** Recording-only response-15 payload. Native 950 uses the rebuild player initializer. */
internal class ReconnectDecoder : ProxyMessageDecoder<Reconnect> {
    override val prot = GameServerProt.RECONNECT

    override fun decode(buffer: JagByteBuf, session: Session): Reconnect {
        val init = checkNotNull(session.readPlayerInfoInit(buffer, reconnect = true))
        require(buffer.readableBytes() == 0) { "Unconsumed reconnect initialization" }
        session.initializePlayerInfo(init)
        return Reconnect(init)
    }
}
