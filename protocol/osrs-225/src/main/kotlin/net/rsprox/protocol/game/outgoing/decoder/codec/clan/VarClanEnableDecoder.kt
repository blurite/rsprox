package net.rsprox.protocol.game.outgoing.decoder.codec.clan

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.game.outgoing.model.clan.VarClanEnable
import net.rsprox.protocol.session.Session

@Consistent
public class VarClanEnableDecoder : ProxyMessageDecoder<VarClanEnable> {
    override val prot: ClientProt = GameServerProt.VARCLAN_ENABLE

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): VarClanEnable {
        return VarClanEnable
    }
}
