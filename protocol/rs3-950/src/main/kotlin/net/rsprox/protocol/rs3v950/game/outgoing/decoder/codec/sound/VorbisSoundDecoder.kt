package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.sound

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.sound.VorbisSound
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class VorbisSoundDecoder : ProxyMessageDecoder<VorbisSound> {
    override val prot: ClientProt = GameServerProt.VORBIS_SOUND

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): VorbisSound {
        val soundId = buffer.g4()
        val loops = buffer.g1()
        val delay = buffer.g2()
        val volume = buffer.g1()
        val pitch = buffer.g2()
        return VorbisSound(
            soundId,
            loops,
            delay,
            volume,
            pitch,
        )
    }
}
