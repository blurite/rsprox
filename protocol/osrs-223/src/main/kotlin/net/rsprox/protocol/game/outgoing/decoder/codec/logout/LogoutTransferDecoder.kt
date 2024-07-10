package net.rsprox.protocol.game.outgoing.decoder.codec.logout

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.metadata.Consistent
import net.rsprot.protocol.tools.MessageDecodingTools
import net.rsprox.protocol.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.game.outgoing.model.logout.LogoutTransfer

@Consistent
public class LogoutTransferDecoder : MessageDecoder<LogoutTransfer> {
    override val prot: ClientProt = GameServerProt.LOGOUT_TRANSFER

    override fun decode(
        buffer: JagByteBuf,
        tools: MessageDecodingTools,
    ): LogoutTransfer {
        val host = buffer.gjstr()
        val id = buffer.g2()
        val properties = buffer.g4()
        return LogoutTransfer(
            host,
            id,
            properties,
        )
    }
}
