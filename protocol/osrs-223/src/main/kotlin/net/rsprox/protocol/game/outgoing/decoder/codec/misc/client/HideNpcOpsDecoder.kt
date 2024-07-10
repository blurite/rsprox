package net.rsprox.protocol.game.outgoing.decoder.codec.misc.client

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.metadata.Consistent
import net.rsprot.protocol.tools.MessageDecodingTools
import net.rsprox.protocol.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.game.outgoing.model.misc.client.HideNpcOps

@Consistent
public class HideNpcOpsDecoder : MessageDecoder<HideNpcOps> {
    override val prot: ClientProt = GameServerProt.HIDENPCOPS

    override fun decode(
        buffer: JagByteBuf,
        tools: MessageDecodingTools,
    ): HideNpcOps {
        val hidden = buffer.gboolean()
        return HideNpcOps(hidden)
    }
}
