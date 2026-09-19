package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.social

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.social.SocialNetworkLogout
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.readNativeIsaacString
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class SocialNetworkLogoutDecoder(
    private val cipher: () -> StreamCipher?,
) : ProxyMessageDecoder<SocialNetworkLogout> {
    override val prot: ClientProt = GameServerProt.SOCIAL_NETWORK_LOGOUT

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): SocialNetworkLogout {
        val activeCipher = checkNotNull(cipher()) { "SOCIAL_NETWORK_LOGOUT requires the inbound opcode cipher" }
        return SocialNetworkLogout(buffer.readNativeIsaacString(activeCipher, buffer.readableBytes().toLong()))
    }
}
