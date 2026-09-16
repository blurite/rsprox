package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.sound

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.sound.VorbisSoundGroupStop
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class VorbisSoundGroupStopDecoder : ProxyMessageDecoder<VorbisSoundGroupStop> {
    override val prot: ClientProt = GameServerProt.VORBIS_SOUND_GROUP_STOP

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): VorbisSoundGroupStop {
        val group = buffer.g2()
        return VorbisSoundGroupStop(
            group = group,
        )
    }
}
