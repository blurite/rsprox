package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.interfaces.IfSetPlayerModelSnapshot
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class IfSetPlayerModelSnapshotDecoder : ProxyMessageDecoder<IfSetPlayerModelSnapshot> {
    override val prot: ClientProt = GameServerProt.IF_SETPLAYERMODEL_SNAPSHOT

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): IfSetPlayerModelSnapshot {
        val componentHash = buffer.g4Alt3().toLong() and 0xFFFFFFFFL
        val snapshotSlot = buffer.g1()
        return IfSetPlayerModelSnapshot(
            componentHash,
            snapshotSlot,
        )
    }
}
