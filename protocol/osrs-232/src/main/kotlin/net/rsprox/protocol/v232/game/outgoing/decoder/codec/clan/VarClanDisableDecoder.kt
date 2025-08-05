package net.rsprox.protocol.v232.game.outgoing.decoder.codec.clan

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.clan.VarClanDisable
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v232.game.outgoing.decoder.prot.GameServerProt

@Consistent
internal class VarClanDisableDecoder : ProxyMessageDecoder<VarClanDisable> {
    override val prot: ClientProt = GameServerProt.VARCLAN_DISABLE

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): VarClanDisable {
        return VarClanDisable
    }
}
