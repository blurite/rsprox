package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.sound

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.sound.MidiJingle
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class MidiJingleDecoder : ProxyMessageDecoder<MidiJingle> {
    override val prot: ClientProt = GameServerProt.MIDI_JINGLE

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): MidiJingle {
        val song = buffer.g4Alt2()
        val volume = buffer.g1Alt1()
        return MidiJingle(
            song = song,
            volume = volume,
        )
    }
}
