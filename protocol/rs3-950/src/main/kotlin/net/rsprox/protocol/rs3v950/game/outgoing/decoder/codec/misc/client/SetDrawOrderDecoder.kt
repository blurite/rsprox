package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.misc.client

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.misc.client.SetDrawOrder
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class SetDrawOrderDecoder : ProxyMessageDecoder<SetDrawOrder> {
    override val prot: ClientProt = GameServerProt.SETDRAWORDER

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): SetDrawOrder {
        val order = buffer.g1()
        return SetDrawOrder(
            order,
        )
    }
}
