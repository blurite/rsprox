package net.rsprox.protocol.v224.game.outgoing.decoder.codec.misc.client

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.misc.client.UpdateRebootTimerV1
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v224.game.outgoing.decoder.prot.GameServerProt

internal class UpdateRebootTimerDecoder : ProxyMessageDecoder<UpdateRebootTimerV1> {
    override val prot: ClientProt = GameServerProt.UPDATE_REBOOT_TIMER_V1

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): UpdateRebootTimerV1 {
        val gameCycles = buffer.g2()
        return UpdateRebootTimerV1(
            gameCycles,
        )
    }
}
