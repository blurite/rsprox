package net.rsprox.protocol.game.outgoing.model.info.worldentityinfo

public class WorldEntityInfoV1(
    override val updates: Map<Int, WorldEntityUpdateType>,
) : WorldEntityInfo {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as WorldEntityInfoV1

        return updates == other.updates
    }

    override fun hashCode(): Int {
        return updates.hashCode()
    }

    override fun toString(): String {
        return "WorldEntityInfoV1(updates=$updates)"
    }
}
