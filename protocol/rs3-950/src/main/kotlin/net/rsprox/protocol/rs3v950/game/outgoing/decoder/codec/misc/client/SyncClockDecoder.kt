package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.misc.client

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.misc.client.SyncClock
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class SyncClockDecoder : ProxyMessageDecoder<SyncClock> {
    override val prot: ClientProt = GameServerProt.SYNC_CLOCK

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): SyncClock {
        val serverTime = buffer.g8()
        return SyncClock(
            serverTime,
        )
    }
}
