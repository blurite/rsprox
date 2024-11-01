package net.rsprox.protocol.game.incoming.decoder.codec.misc.client
import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.incoming.decoder.prot.GameClientProt
import net.rsprox.protocol.game.incoming.model.misc.client.SoundJingleEnd
import net.rsprox.protocol.session.Session

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
