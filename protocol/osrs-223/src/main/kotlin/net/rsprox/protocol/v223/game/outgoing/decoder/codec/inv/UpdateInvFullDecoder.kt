package net.rsprox.protocol.v223.game.outgoing.decoder.codec.inv

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.util.gCombinedId
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.inv.UpdateInvFull
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v223.game.outgoing.decoder.prot.GameServerProt

internal class UpdateInvFullDecoder : ProxyMessageDecoder<UpdateInvFull> {
    override val prot: ClientProt = GameServerProt.UPDATE_INV_FULL

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): UpdateInvFull {
        val combinedId = buffer.gCombinedId()
        val inventoryId = buffer.g2()
        val capacity = buffer.g2()
        val objs =
            buildList {
                for (i in 0..<capacity) {
                    var count = buffer.g1Alt2()
                    if (count >= 0xFF) {
                        count = buffer.g4Alt3()
                    }
                    val id = buffer.g2()
                    add(UpdateInvFull.Obj(id - 1, count))
                }
            }
        return UpdateInvFull(
            combinedId.interfaceId,
            combinedId.componentId,
            inventoryId,
            objs,
        )
    }
}
