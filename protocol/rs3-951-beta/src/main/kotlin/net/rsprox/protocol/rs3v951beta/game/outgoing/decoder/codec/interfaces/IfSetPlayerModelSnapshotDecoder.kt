package net.rsprox.protocol.rs3v951beta.game.outgoing.decoder.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.interfaces.IfSetPlayerModelSnapshot
import net.rsprox.protocol.rs3v951beta.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class IfSetPlayerModelSnapshotDecoder : ProxyMessageDecoder<IfSetPlayerModelSnapshot> {
    override val prot: ClientProt = GameServerProt.IF_SETPLAYERMODEL_SNAPSHOT

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): IfSetPlayerModelSnapshot {
        val snapshotSlot = buffer.g1Alt3()
        val componentHash = buffer.g4Alt2().toLong() and 0xFFFF_FFFFL
        return IfSetPlayerModelSnapshot(
            componentHash,
            snapshotSlot,
        )
    }
}
