package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.inv

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.inv.UpdateInvStopTransmit
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class UpdateInvStopTransmitDecoder : ProxyMessageDecoder<UpdateInvStopTransmit> {
    override val prot: ClientProt = GameServerProt.UPDATE_INV_STOP_TRANSMIT

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): UpdateInvStopTransmit {
        val flags = buffer.g1Alt3()
        val inventoryId = buffer.g2Alt2()
        return UpdateInvStopTransmit(
            inventoryId,
            flags,
        )
    }
}
