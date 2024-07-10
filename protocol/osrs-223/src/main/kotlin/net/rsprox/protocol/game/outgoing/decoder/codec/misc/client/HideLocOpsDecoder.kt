package net.rsprox.protocol.game.outgoing.decoder.codec.misc.client

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.metadata.Consistent
import net.rsprot.protocol.tools.MessageDecodingTools
import net.rsprox.protocol.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.game.outgoing.model.misc.client.HideLocOps

@Consistent
public class HideLocOpsDecoder : MessageDecoder<HideLocOps> {
    override val prot: ClientProt = GameServerProt.HIDELOCOPS

    override fun decode(
        buffer: JagByteBuf,
        tools: MessageDecodingTools,
    ): HideLocOps {
        val hidden = buffer.gboolean()
        return HideLocOps(hidden)
    }
}
