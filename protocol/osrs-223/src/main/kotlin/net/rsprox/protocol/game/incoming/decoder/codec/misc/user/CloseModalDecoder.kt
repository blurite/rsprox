package net.rsprox.protocol.game.incoming.decoder.codec.misc.user

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.metadata.Consistent
import net.rsprot.protocol.tools.MessageDecodingTools
import net.rsprox.protocol.game.incoming.decoder.prot.GameClientProt
import net.rsprox.protocol.game.incoming.model.misc.user.CloseModal

@Consistent
public class CloseModalDecoder : MessageDecoder<CloseModal> {
    override val prot: ClientProt = GameClientProt.CLOSE_MODAL

    override fun decode(
        buffer: JagByteBuf,
        tools: MessageDecodingTools,
    ): CloseModal {
        return CloseModal
    }
}
