package net.rsprox.protocol.game.outgoing.decoder.codec.sound

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.tools.MessageDecodingTools
import net.rsprox.protocol.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.game.outgoing.model.sound.MidiJingle

public class MidiJingleDecoder : MessageDecoder<MidiJingle> {
    override val prot: ClientProt = GameServerProt.MIDI_JINGLE

    override fun decode(
        buffer: JagByteBuf,
        tools: MessageDecodingTools,
    ): MidiJingle {
        val id = buffer.g2Alt3()
        val lengthInMillis = buffer.g3Alt2()
        return MidiJingle(
            id,
            lengthInMillis,
        )
    }
}
