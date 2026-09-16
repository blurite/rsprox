package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.sound

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.sound.SongPreload
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class SongPreloadDecoder : ProxyMessageDecoder<SongPreload> {
    override val prot: ClientProt = GameServerProt.SONG_PRELOAD

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): SongPreload {
        val id = buffer.g4Alt2()
        return SongPreload(
            id = id,
        )
    }
}
