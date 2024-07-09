package net.rsprox.protocol.game.incoming.decoder.codec.buttons

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.metadata.Consistent
import net.rsprot.protocol.tools.MessageDecodingTools
import net.rsprot.protocol.util.gCombinedId
import net.rsprox.protocol.game.incoming.decoder.prot.GameClientProt
import net.rsprox.protocol.game.incoming.model.buttons.If1Button

@Consistent
public class If1ButtonDecoder : MessageDecoder<If1Button> {
    override val prot: ClientProt = GameClientProt.IF_BUTTON

    override fun decode(
        buffer: JagByteBuf,
        tools: MessageDecodingTools,
    ): If1Button {
        val combinedId = buffer.gCombinedId()
        return If1Button(combinedId)
    }
}
