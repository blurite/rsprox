package net.rsprox.protocol.game.incoming.decoder.codec.misc.client

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.metadata.Consistent
import net.rsprot.protocol.tools.MessageDecodingTools
import net.rsprox.protocol.game.incoming.decoder.prot.GameClientProt
import net.rsprox.protocol.game.incoming.model.misc.client.SoundJingleEnd

@Consistent
public class SoundJingleEndDecoder : MessageDecoder<SoundJingleEnd> {
    override val prot: ClientProt = GameClientProt.SOUND_JINGLEEND

    override fun decode(
        buffer: JagByteBuf,
        tools: MessageDecodingTools,
    ): SoundJingleEnd {
        val jingle = buffer.g4()
        return SoundJingleEnd(jingle)
    }
}
