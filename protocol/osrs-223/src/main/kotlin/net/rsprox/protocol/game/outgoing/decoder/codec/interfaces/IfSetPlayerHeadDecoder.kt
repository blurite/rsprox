package net.rsprox.protocol.game.outgoing.decoder.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.tools.MessageDecodingTools
import net.rsprot.protocol.util.gCombinedId
import net.rsprox.protocol.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.game.outgoing.model.interfaces.IfSetPlayerHead

public class IfSetPlayerHeadDecoder : MessageDecoder<IfSetPlayerHead> {
    override val prot: ClientProt = GameServerProt.IF_SETPLAYERHEAD

    override fun decode(
        buffer: JagByteBuf,
        tools: MessageDecodingTools,
    ): IfSetPlayerHead {
        val combinedId = buffer.gCombinedId()
        return IfSetPlayerHead(
            combinedId.interfaceId,
            combinedId.componentId,
        )
    }
}
