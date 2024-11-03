package net.rsprox.protocol.v224.game.outgoing.decoder.codec.sound

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.sound.MidiJingle
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v224.game.outgoing.decoder.prot.GameServerProt

internal class MidiJingleDecoder : ProxyMessageDecoder<MidiJingle> {
    override val prot: ClientProt = GameServerProt.MIDI_JINGLE

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): MidiJingle {
        val lengthInMillis = buffer.g3Alt2()
        val id = buffer.g2Alt3()
        return MidiJingle(
            id,
            lengthInMillis,
        )
    }
}
