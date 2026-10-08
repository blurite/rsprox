package net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.interfaces.IfSetPlayerHeadSnapshot
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class IfSetPlayerHeadSnapshotDecoder : ProxyMessageDecoder<IfSetPlayerHeadSnapshot> {
    override val prot: ClientProt = GameServerProt.IF_SETPLAYERHEAD_SNAPSHOT

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): IfSetPlayerHeadSnapshot {
        val componentHash = buffer.g4().toLong() and 0xFFFF_FFFFL
        val snapshotSlot = buffer.g1Alt1()
        return IfSetPlayerHeadSnapshot(
            componentHash,
            snapshotSlot,
        )
    }
}
