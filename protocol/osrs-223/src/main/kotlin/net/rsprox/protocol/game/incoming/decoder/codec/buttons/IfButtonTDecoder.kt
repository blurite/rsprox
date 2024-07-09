package net.rsprox.protocol.game.incoming.decoder.codec.buttons

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.tools.MessageDecodingTools
import net.rsprot.protocol.util.gCombinedIdAlt2
import net.rsprot.protocol.util.gCombinedIdAlt3
import net.rsprox.protocol.game.incoming.decoder.prot.GameClientProt
import net.rsprox.protocol.game.incoming.model.buttons.IfButtonT

public class IfButtonTDecoder : MessageDecoder<IfButtonT> {
    override val prot: ClientProt = GameClientProt.IF_BUTTONT

    override fun decode(
        buffer: JagByteBuf,
        tools: MessageDecodingTools,
    ): IfButtonT {
        val selectedCombinedId = buffer.gCombinedIdAlt2()
        val targetCombinedId = buffer.gCombinedIdAlt3()
        val selectedObj = buffer.g2Alt3()
        val selectedSub = buffer.g2Alt1()
        val targetSub = buffer.g2Alt2()
        val targetObj = buffer.g2Alt3()
        return IfButtonT(
            selectedCombinedId,
            selectedSub,
            selectedObj,
            targetCombinedId,
            targetSub,
            targetObj,
        )
    }
}
