package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.misc.client

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.misc.client.UpdateRebootTimer
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class UpdateRebootTimerDecoder : ProxyMessageDecoder<UpdateRebootTimer> {
    override val prot: ClientProt = GameServerProt.UPDATE_REBOOT_TIMER

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): UpdateRebootTimer {
        val duration = buffer.g2()
        return UpdateRebootTimer(
            duration,
        )
    }
}
