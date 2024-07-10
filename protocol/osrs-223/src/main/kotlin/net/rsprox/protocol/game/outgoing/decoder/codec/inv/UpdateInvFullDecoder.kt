package net.rsprox.protocol.game.outgoing.decoder.codec.inv

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.tools.MessageDecodingTools
import net.rsprot.protocol.util.gCombinedId
import net.rsprox.protocol.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.game.outgoing.model.inv.UpdateInvFull

public class UpdateInvFullDecoder : MessageDecoder<UpdateInvFull> {
    override val prot: ClientProt = GameServerProt.UPDATE_INV_FULL

    override fun decode(
        buffer: JagByteBuf,
        tools: MessageDecodingTools,
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
                    add(UpdateInvFull.Obj(id, count))
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
