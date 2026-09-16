package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.group

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.cache.api.rs3.Rs3VariableDomain
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.group.PlayerGroupVarps
import net.rsprox.protocol.rs3v950.cache.readTypedVariable
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class PlayerGroupVarpsDecoder : ProxyMessageDecoder<PlayerGroupVarps> {
    override val prot: ClientProt = GameServerProt.PLAYER_GROUP_VARPS

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): PlayerGroupVarps {
        val member = buffer.g2()
        val clear = buffer.g1()
        val variables =
            buildList {
                while (buffer.isReadable) add(buffer.readTypedVariable(session, Rs3VariableDomain.PLAYER))
            }
        return PlayerGroupVarps(member, clear, variables)
    }
}
