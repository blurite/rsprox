package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.misc.player

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.misc.player.ClearPlayerSnapshot
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class ClearPlayerSnapshotDecoder : ProxyMessageDecoder<ClearPlayerSnapshot> {
    override val prot: ClientProt = GameServerProt.CLEAR_PLAYER_SNAPSHOT

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): ClearPlayerSnapshot {
        val index = buffer.g1()
        return ClearPlayerSnapshot(
            index,
        )
    }
}
