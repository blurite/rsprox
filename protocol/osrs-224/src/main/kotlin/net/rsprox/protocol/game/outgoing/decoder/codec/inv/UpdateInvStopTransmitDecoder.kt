package net.rsprox.protocol.game.outgoing.decoder.codec.inv

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.game.outgoing.model.inv.UpdateInvStopTransmit
import net.rsprox.protocol.session.Session

@Consistent
public class UpdateInvStopTransmitDecoder : ProxyMessageDecoder<UpdateInvStopTransmit> {
    override val prot: ClientProt = GameServerProt.UPDATE_INV_STOPTRANSMIT

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): UpdateInvStopTransmit {
        val inventoryId = buffer.g2Alt2()
        return UpdateInvStopTransmit(inventoryId)
    }
}
