package net.rsprox.protocol.game.outgoing.decoder.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.metadata.Consistent
import net.rsprot.protocol.tools.MessageDecodingTools
import net.rsprot.protocol.util.gCombinedId
import net.rsprox.protocol.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.game.outgoing.model.interfaces.IfCloseSub

@Consistent
public class IfCloseSubDecoder : MessageDecoder<IfCloseSub> {
    override val prot: ClientProt = GameServerProt.IF_CLOSESUB

    override fun decode(
        buffer: JagByteBuf,
        tools: MessageDecodingTools,
    ): IfCloseSub {
        val combinedId = buffer.gCombinedId()
        return IfCloseSub(
            combinedId.interfaceId,
            combinedId.componentId,
        )
    }
}
