package net.rsprox.protocol.game.outgoing.decoder.codec.varp

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.metadata.Consistent
import net.rsprot.protocol.tools.MessageDecodingTools
import net.rsprox.protocol.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.game.outgoing.model.varp.VarpSync

@Consistent
public class VarpSyncDecoder : MessageDecoder<VarpSync> {
    override val prot: ClientProt = GameServerProt.VARP_SYNC

    override fun decode(
        buffer: JagByteBuf,
        tools: MessageDecodingTools,
    ): VarpSync {
        return VarpSync
    }
}
