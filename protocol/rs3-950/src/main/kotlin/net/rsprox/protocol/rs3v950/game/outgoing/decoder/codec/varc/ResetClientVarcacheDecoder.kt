package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.varc

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.varc.ResetClientVarcache
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.info.npcinfo.npcMorphVariables
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

@Consistent
internal class ResetClientVarcacheDecoder : ProxyMessageDecoder<ResetClientVarcache> {
    override val prot: ClientProt = GameServerProt.RESET_CLIENT_VARCACHE

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): ResetClientVarcache {
        session.npcMorphVariables().clear()
        return ResetClientVarcache
    }
}
