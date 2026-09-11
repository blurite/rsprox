package net.rsprox.protocol.rs3.game.outgoing.model.interfaces

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public class IfSetPlayerModelSnapshot(
    public val componentHash: Long,
    public val snapshotSlot: Int,
) : IncomingServerGameMessage {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as IfSetPlayerModelSnapshot

        if (componentHash != other.componentHash) return false
        if (snapshotSlot != other.snapshotSlot) return false

        return true
    }

    override fun hashCode(): Int {
        var result = componentHash.hashCode()
        result = 31 * result + snapshotSlot
        return result
    }

    override fun toString(): String {
        return "IfSetPlayerModelSnapshot(componentHash=$componentHash, snapshotSlot=$snapshotSlot)"
    }
}
