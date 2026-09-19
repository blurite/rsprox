package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.sound

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.sound.VorbisPreloadSounds
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class VorbisPreloadSoundsDecoder : ProxyMessageDecoder<VorbisPreloadSounds> {
    override val prot: ClientProt = GameServerProt.VORBIS_PRELOAD_SOUNDS

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): VorbisPreloadSounds {
        val id = buffer.g4()
        return VorbisPreloadSounds(
            id = id,
        )
    }
}
