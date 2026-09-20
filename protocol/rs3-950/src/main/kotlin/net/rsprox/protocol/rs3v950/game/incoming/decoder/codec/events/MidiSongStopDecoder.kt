package net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.events

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.incoming.model.events.MidiSongStop
import net.rsprox.protocol.session.Session

internal class MidiSongStopDecoder(
    override val prot: ClientProt,
) : ProxyMessageDecoder<MidiSongStop> {
    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): MidiSongStop {
        val song = buffer.g4()
        return MidiSongStop(
            song,
        )
    }
}
