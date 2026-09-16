package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.sound

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.sound.SoundMixbussSetLevel
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class SoundMixbussSetLevelDecoder : ProxyMessageDecoder<SoundMixbussSetLevel> {
    override val prot: ClientProt = GameServerProt.SOUND_MIXBUSS_SETLEVEL

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): SoundMixbussSetLevel {
        val bus = buffer.g2()
        val level = buffer.g2()
        return SoundMixbussSetLevel(
            bus = bus,
            level = level,
        )
    }
}
