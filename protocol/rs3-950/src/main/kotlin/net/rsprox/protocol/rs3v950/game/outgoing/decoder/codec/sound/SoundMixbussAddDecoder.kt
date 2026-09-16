package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.sound

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.sound.SoundMixbussAdd
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class SoundMixbussAddDecoder : ProxyMessageDecoder<SoundMixbussAdd> {
    override val prot: ClientProt = GameServerProt.SOUND_MIXBUSS_ADD

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): SoundMixbussAdd {
        val bus = buffer.g2()
        val parent = buffer.g2()
        val level = buffer.g2()
        return SoundMixbussAdd(
            bus = bus,
            parent = parent,
            level = level,
        )
    }
}
