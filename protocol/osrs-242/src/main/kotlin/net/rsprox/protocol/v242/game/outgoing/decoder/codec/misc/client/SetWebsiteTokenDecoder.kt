package net.rsprox.protocol.v242.game.outgoing.decoder.codec.misc.client

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.misc.client.SetWebsiteToken
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v242.game.outgoing.decoder.prot.GameServerProt

@Consistent
internal class SetWebsiteTokenDecoder : ProxyMessageDecoder<SetWebsiteToken> {
    override val prot: ClientProt = GameServerProt.SET_WEBSITE_TOKEN

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): SetWebsiteToken {
        val token = buffer.gjstr()
        return SetWebsiteToken(
            token,
        )
    }
}
