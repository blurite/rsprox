package net.rsprox.protocol.game.outgoing.decoder.codec.misc.client

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.game.outgoing.model.misc.client.UpdateRebootTimer
import net.rsprox.protocol.session.Session

public class UpdateRebootTimerDecoder : ProxyMessageDecoder<UpdateRebootTimer> {
    override val prot: ClientProt = GameServerProt.UPDATE_REBOOT_TIMER

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): UpdateRebootTimer {
        val gameCycles = buffer.g2Alt2()
        return UpdateRebootTimer(
            gameCycles,
        )
    }
}
