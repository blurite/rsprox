package net.rsprox.protocol.game.outgoing.decoder.codec.varp

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.tools.MessageDecodingTools
import net.rsprox.protocol.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.game.outgoing.model.varp.VarpLarge

public class VarpLargeDecoder : MessageDecoder<VarpLarge> {
    override val prot: ClientProt = GameServerProt.VARP_LARGE

    override fun decode(
        buffer: JagByteBuf,
        tools: MessageDecodingTools,
    ): VarpLarge {
        val id = buffer.g2Alt3()
        val value = buffer.g4Alt2()
        return VarpLarge(
            id,
            value,
        )
    }
}
