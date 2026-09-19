package net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.misc.client

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.incoming.model.misc.client.UnnamedLobbyRequest
import net.rsprox.protocol.session.Session

internal class UnnamedLobbyRequestDecoder(
    override val prot: ClientProt,
) : ProxyMessageDecoder<UnnamedLobbyRequest> {
    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): UnnamedLobbyRequest {
        return UnnamedLobbyRequest
    }
}
