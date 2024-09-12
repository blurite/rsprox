package net.rsprox.protocol.game.outgoing.decoder.codec.unknown

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.game.outgoing.model.unknown.UnknownString
import net.rsprox.protocol.session.Session

@Consistent
public class UnknownStringDecoder : ProxyMessageDecoder<UnknownString> {
    override val prot: ClientProt = GameServerProt.UNKNOWN_STRING

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): UnknownString {
        val string = buffer.gjstr()
        return UnknownString(string)
    }
}
