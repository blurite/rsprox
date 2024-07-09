package net.rsprox.protocol.game.incoming.decoder.codec.social

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.metadata.Consistent
import net.rsprot.protocol.tools.MessageDecodingTools
import net.rsprox.protocol.game.incoming.decoder.prot.GameClientProt
import net.rsprox.protocol.game.incoming.model.social.IgnoreListAdd

@Consistent
public class IgnoreListAddDecoder : MessageDecoder<IgnoreListAdd> {
    override val prot: ClientProt = GameClientProt.IGNORELIST_ADD

    override fun decode(
        buffer: JagByteBuf,
        tools: MessageDecodingTools,
    ): IgnoreListAdd {
        val name = buffer.gjstr()
        return IgnoreListAdd(name)
    }
}
