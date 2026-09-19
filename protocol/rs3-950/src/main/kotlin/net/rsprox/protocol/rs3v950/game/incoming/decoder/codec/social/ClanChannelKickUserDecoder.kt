package net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.social

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.incoming.model.social.ClanChannelKickUser
import net.rsprox.protocol.rs3v950.buffer.readNativeString
import net.rsprox.protocol.session.Session

internal class ClanChannelKickUserDecoder(
    override val prot: ClientProt,
) : ProxyMessageDecoder<ClanChannelKickUser> {
    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): ClanChannelKickUser {
        val channel = buffer.g1()
        val member = buffer.g2()
        val name = buffer.readNativeString()
        return ClanChannelKickUser(
            channel,
            member,
            name,
        )
    }
}
