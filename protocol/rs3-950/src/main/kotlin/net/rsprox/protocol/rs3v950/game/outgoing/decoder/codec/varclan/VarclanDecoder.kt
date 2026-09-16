package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.varclan

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.cache.api.rs3.Rs3VariableDomain
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.varclan.Varclan
import net.rsprox.protocol.rs3v950.cache.readTypedVariable
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class VarclanDecoder : ProxyMessageDecoder<Varclan> {
    override val prot: ClientProt = GameServerProt.VARCLAN

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): Varclan {
        val variable = buffer.readTypedVariable(session, Rs3VariableDomain.CLAN)
        return Varclan(variable.id, variable)
    }
}
