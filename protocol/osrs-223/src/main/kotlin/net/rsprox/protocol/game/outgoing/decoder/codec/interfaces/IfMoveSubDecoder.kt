package net.rsprox.protocol.game.outgoing.decoder.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.tools.MessageDecodingTools
import net.rsprot.protocol.util.gCombinedIdAlt1
import net.rsprox.protocol.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.game.outgoing.model.interfaces.IfMoveSub

public class IfMoveSubDecoder : MessageDecoder<IfMoveSub> {
    override val prot: ClientProt = GameServerProt.IF_MOVESUB

    override fun decode(
        buffer: JagByteBuf,
        tools: MessageDecodingTools,
    ): IfMoveSub {
        val destinationCombinedId = buffer.gCombinedIdAlt1()
        val sourceCombinedId = buffer.gCombinedIdAlt1()
        return IfMoveSub(
            sourceCombinedId.interfaceId,
            sourceCombinedId.componentId,
            destinationCombinedId.interfaceId,
            destinationCombinedId.componentId,
        )
    }
}
