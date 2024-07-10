package net.rsprox.protocol.game.outgoing.decoder.codec.sound

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.metadata.Consistent
import net.rsprot.protocol.tools.MessageDecodingTools
import net.rsprox.protocol.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.game.outgoing.model.sound.SynthSound

@Consistent
public class SynthSoundDecoder : MessageDecoder<SynthSound> {
    override val prot: ClientProt = GameServerProt.SYNTH_SOUND

    override fun decode(
        buffer: JagByteBuf,
        tools: MessageDecodingTools,
    ): SynthSound {
        val id = buffer.g2()
        val loops = buffer.g1()
        val delay = buffer.g2()
        return SynthSound(
            id,
            loops,
            delay,
        )
    }
}
