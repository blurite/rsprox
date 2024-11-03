package net.rsprox.protocol.v226.game.outgoing.decoder.codec.sound

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.v226.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.game.outgoing.model.sound.MidiJingle
import net.rsprox.protocol.session.Session

public class MidiJingleDecoder : ProxyMessageDecoder<MidiJingle> {
    override val prot: ClientProt = GameServerProt.MIDI_JINGLE

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): MidiJingle {
        val id = buffer.g2Alt1()
        val lengthInMillis = buffer.g3Alt3()
        return MidiJingle(
            id,
            lengthInMillis,
        )
    }
}
