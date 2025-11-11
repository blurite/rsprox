package net.rsprox.protocol.v234.game.outgoing.decoder.codec.clan

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.clan.VarClan
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v234.game.outgoing.decoder.prot.GameServerProt

@Consistent
internal class VarClanDecoder : ProxyMessageDecoder<VarClan> {
    override val prot: ClientProt = GameServerProt.VARCLAN

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): VarClan {
        val id = buffer.g2()
        val data = ByteArray(buffer.readableBytes())
        buffer.gdata(data)
        return VarClan(id, VarClan.UnknownVarClanData(data))
    }
}
