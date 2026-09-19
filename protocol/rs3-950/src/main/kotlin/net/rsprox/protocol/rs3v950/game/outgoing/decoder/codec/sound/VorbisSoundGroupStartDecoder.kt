package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.sound

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.sound.VorbisSoundGroupStart
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class VorbisSoundGroupStartDecoder : ProxyMessageDecoder<VorbisSoundGroupStart> {
    override val prot: ClientProt = GameServerProt.VORBIS_SOUND_GROUP_START

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): VorbisSoundGroupStart {
        val group = buffer.g2()
        return VorbisSoundGroupStart(
            group = group,
        )
    }
}
