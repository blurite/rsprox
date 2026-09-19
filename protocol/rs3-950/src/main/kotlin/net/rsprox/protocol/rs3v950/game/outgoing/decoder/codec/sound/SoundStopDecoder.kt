package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.sound

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.sound.SoundStop
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class SoundStopDecoder : ProxyMessageDecoder<SoundStop> {
    override val prot: ClientProt = GameServerProt.SOUND_STOP

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): SoundStop {
        val id = buffer.g4()
        val group = buffer.g2()
        return SoundStop(
            id = id,
            group = group,
        )
    }
}
