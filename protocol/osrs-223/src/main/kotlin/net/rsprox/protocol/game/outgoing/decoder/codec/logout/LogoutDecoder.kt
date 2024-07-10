package net.rsprox.protocol.game.outgoing.decoder.codec.logout

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.metadata.Consistent
import net.rsprot.protocol.tools.MessageDecodingTools
import net.rsprox.protocol.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.game.outgoing.model.logout.Logout

@Consistent
public class LogoutDecoder : MessageDecoder<Logout> {
    override val prot: ClientProt = GameServerProt.LOGOUT

    override fun decode(
        buffer: JagByteBuf,
        tools: MessageDecodingTools,
    ): Logout {
        return Logout
    }
}
