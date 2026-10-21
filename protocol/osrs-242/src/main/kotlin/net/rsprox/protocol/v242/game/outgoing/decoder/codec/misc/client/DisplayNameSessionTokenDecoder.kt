package net.rsprox.protocol.v242.game.outgoing.decoder.codec.misc.client

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.misc.client.DisplayNameSessionToken
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v242.game.outgoing.decoder.prot.GameServerProt

@Consistent
internal class DisplayNameSessionTokenDecoder : ProxyMessageDecoder<DisplayNameSessionToken> {
    override val prot: ClientProt = GameServerProt.DISPLAYNAME_SESSION_TOKEN

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): DisplayNameSessionToken {
        val token = buffer.gjstr()
        return DisplayNameSessionToken(
            token,
        )
    }
}
