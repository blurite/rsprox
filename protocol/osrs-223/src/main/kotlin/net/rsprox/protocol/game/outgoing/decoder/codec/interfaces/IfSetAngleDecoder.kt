package net.rsprox.protocol.game.outgoing.decoder.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.tools.MessageDecodingTools
import net.rsprot.protocol.util.gCombinedId
import net.rsprox.protocol.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.game.outgoing.model.interfaces.IfSetAngle

public class IfSetAngleDecoder : MessageDecoder<IfSetAngle> {
    override val prot: ClientProt = GameServerProt.IF_SETANGLE

    override fun decode(
        buffer: JagByteBuf,
        tools: MessageDecodingTools,
    ): IfSetAngle {
        val combinedId = buffer.gCombinedId()
        val angleX = buffer.g2Alt3()
        val angleY = buffer.g2()
        val zoom = buffer.g2()
        return IfSetAngle(
            combinedId.interfaceId,
            combinedId.componentId,
            angleX,
            angleY,
            zoom,
        )
    }
}
