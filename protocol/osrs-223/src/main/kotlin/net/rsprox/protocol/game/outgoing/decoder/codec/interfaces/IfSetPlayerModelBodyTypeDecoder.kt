package net.rsprox.protocol.game.outgoing.decoder.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.tools.MessageDecodingTools
import net.rsprot.protocol.util.gCombinedIdAlt3
import net.rsprox.protocol.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.game.outgoing.model.interfaces.IfSetPlayerModelBodyType

public class IfSetPlayerModelBodyTypeDecoder : MessageDecoder<IfSetPlayerModelBodyType> {
    override val prot: ClientProt = GameServerProt.IF_SETPLAYERMODEL_BODYTYPE

    override fun decode(
        buffer: JagByteBuf,
        tools: MessageDecodingTools,
    ): IfSetPlayerModelBodyType {
        val combinedId = buffer.gCombinedIdAlt3()
        val bodyType = buffer.g1Alt3()
        return IfSetPlayerModelBodyType(
            combinedId.interfaceId,
            combinedId.componentId,
            bodyType,
        )
    }
}
