package net.rsprox.protocol.game.incoming.decoder.codec.misc.client

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.metadata.Consistent
import net.rsprot.protocol.tools.MessageDecodingTools
import net.rsprox.protocol.game.incoming.decoder.prot.GameClientProt
import net.rsprox.protocol.game.incoming.model.misc.client.WindowStatus

@Consistent
public class WindowStatusDecoder : MessageDecoder<WindowStatus> {
    override val prot: ClientProt = GameClientProt.WINDOW_STATUS

    override fun decode(
        buffer: JagByteBuf,
        tools: MessageDecodingTools,
    ): WindowStatus {
        val windowMode = buffer.g1()
        val frameWidth = buffer.g2()
        val frameHeight = buffer.g2()
        return WindowStatus(
            windowMode,
            frameWidth,
            frameHeight,
        )
    }
}
