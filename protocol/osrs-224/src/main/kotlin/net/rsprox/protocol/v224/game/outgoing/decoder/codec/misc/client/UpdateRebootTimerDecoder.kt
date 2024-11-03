package net.rsprox.protocol.v224.game.outgoing.decoder.codec.misc.client

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.misc.client.UpdateRebootTimer
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v224.game.outgoing.decoder.prot.GameServerProt

internal class UpdateRebootTimerDecoder : ProxyMessageDecoder<UpdateRebootTimer> {
    override val prot: ClientProt = GameServerProt.UPDATE_REBOOT_TIMER

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): UpdateRebootTimer {
        val gameCycles = buffer.g2Alt3()
        return UpdateRebootTimer(
            gameCycles,
        )
    }
}
