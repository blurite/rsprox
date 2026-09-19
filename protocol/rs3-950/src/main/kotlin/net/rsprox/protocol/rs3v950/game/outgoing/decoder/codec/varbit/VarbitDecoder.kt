package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.varbit

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.varbit.Varbit
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.gVarIntLE
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.info.npcinfo.updateNpcMorphVarbit
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class VarbitDecoder : ProxyMessageDecoder<Varbit> {
    override val prot: ClientProt = GameServerProt.VARBIT

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): Varbit {
        val id = buffer.gVarIntLE()
        val value = buffer.gVarIntLE()
        session.updateNpcMorphVarbit(id, value)
        return Varbit(
            id = id,
            value = value,
        )
    }
}
