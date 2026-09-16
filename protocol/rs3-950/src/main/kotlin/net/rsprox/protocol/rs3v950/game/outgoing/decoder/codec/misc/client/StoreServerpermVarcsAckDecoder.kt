package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.misc.client

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.misc.client.StoreServerpermVarcsAck
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class StoreServerpermVarcsAckDecoder : ProxyMessageDecoder<StoreServerpermVarcsAck> {
    override val prot: ClientProt = GameServerProt.STORE_SERVERPERM_VARCS_ACK

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): StoreServerpermVarcsAck = StoreServerpermVarcsAck
}
