package net.rsprox.protocol.v233.game.outgoing.decoder.codec.misc.player

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.misc.player.AccountFlags
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v233.game.outgoing.decoder.prot.GameServerProt

@Consistent
internal class AccountFlagsDecoder : ProxyMessageDecoder<AccountFlags> {
    override val prot: ClientProt = GameServerProt.ACCOUNT_FLAGS

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): AccountFlags {
        val flags = buffer.g8()
        return AccountFlags(
            flags,
        )
    }
}
