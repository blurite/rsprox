package net.rsprox.protocol.v236.game.outgoing.decoder.codec.misc.client

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.misc.client.UpdateRebootTimerV2
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v236.game.outgoing.decoder.prot.GameServerProt

internal class UpdateRebootTimerV2Decoder : ProxyMessageDecoder<UpdateRebootTimerV2> {
    override val prot: ClientProt = GameServerProt.UPDATE_REBOOT_TIMER_V2

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): UpdateRebootTimerV2 {
        val message = buffer.gjstr()
        val gameCycles = buffer.g2Alt3()
        return UpdateRebootTimerV2(
            gameCycles,
            when {
                message.isEmpty() -> UpdateRebootTimerV2.IgnoreUpdateMessage
                message == CANCEL -> UpdateRebootTimerV2.ClearUpdateMessage
                else -> UpdateRebootTimerV2.SetUpdateMessage(message)
            }
        )
    }

    private companion object {
        private const val CANCEL: String = "\u0018"
    }
}
