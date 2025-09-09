package net.rsprox.protocol.v233.game.incoming.decoder.codec.misc.client

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.incoming.model.misc.client.SoundJingleEnd
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v233.game.incoming.decoder.prot.GameClientProt

@Consistent
public class SoundJingleEndDecoder : ProxyMessageDecoder<SoundJingleEnd> {
    override val prot: ClientProt = GameClientProt.SOUND_JINGLEEND

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): SoundJingleEnd {
        val jingle = buffer.g4()
        return SoundJingleEnd(jingle)
    }
}
