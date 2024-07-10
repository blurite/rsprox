package net.rsprox.protocol.game.outgoing.decoder.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.tools.MessageDecodingTools
import net.rsprot.protocol.util.gCombinedIdAlt3
import net.rsprox.protocol.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.game.outgoing.model.interfaces.IfSetNpcHeadActive

public class IfSetNpcHeadActiveDecoder : MessageDecoder<IfSetNpcHeadActive> {
    override val prot: ClientProt = GameServerProt.IF_SETNPCHEAD_ACTIVE

    override fun decode(
        buffer: JagByteBuf,
        tools: MessageDecodingTools,
    ): IfSetNpcHeadActive {
        val combinedId = buffer.gCombinedIdAlt3()
        val index = buffer.g2Alt3()
        return IfSetNpcHeadActive(
            combinedId.interfaceId,
            combinedId.componentId,
            index,
        )
    }
}
