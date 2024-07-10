package net.rsprox.protocol.game.outgoing.decoder.codec.misc.client

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.tools.MessageDecodingTools
import net.rsprox.protocol.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.game.outgoing.model.misc.client.UpdateRebootTimer

public class UpdateRebootTimerDecoder : MessageDecoder<UpdateRebootTimer> {
    override val prot: ClientProt = GameServerProt.UPDATE_REBOOT_TIMER

    override fun decode(
        buffer: JagByteBuf,
        tools: MessageDecodingTools,
    ): UpdateRebootTimer {
        val gameCycles = buffer.g2Alt3()
        return UpdateRebootTimer(
            gameCycles,
        )
    }
}
