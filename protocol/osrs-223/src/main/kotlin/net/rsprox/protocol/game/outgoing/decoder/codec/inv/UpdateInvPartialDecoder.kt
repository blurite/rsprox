package net.rsprox.protocol.game.outgoing.decoder.codec.inv

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprot.protocol.util.gCombinedId
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.game.outgoing.model.inv.UpdateInvPartial
import net.rsprox.protocol.session.Session

@Consistent
public class UpdateInvPartialDecoder : ProxyMessageDecoder<UpdateInvPartial> {
    override val prot: ClientProt = GameServerProt.UPDATE_INV_PARTIAL

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): UpdateInvPartial {
        val combinedId = buffer.gCombinedId()
        val inventoryId = buffer.g2()
        val objs =
            buildList {
                while (buffer.isReadable) {
                    val slot = buffer.gSmart1or2()
                    val id = buffer.g2()
                    if (id == 0) {
                        add(UpdateInvPartial.IndexedObj(slot, 0, 0))
                        continue
                    }
                    var count = buffer.g1()
                    if (count >= 0xFF) {
                        count = buffer.g4()
                    }
                    add(UpdateInvPartial.IndexedObj(slot, id - 1, count))
                }
            }
        return UpdateInvPartial(
            combinedId.interfaceId,
            combinedId.componentId,
            inventoryId,
            objs,
        )
    }
}
