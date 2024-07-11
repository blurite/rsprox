package net.rsprox.protocol.game.outgoing.model.playerinfo.util

import net.rsprox.protocol.common.CoordGrid

public class PlayerInfoInitBlock(
    public val localPlayerCoord: CoordGrid,
    private val positions: IntArray,
) {
    private val lowResolutionPositions: Array<LowResolutionPosition>
        get() =
            Array(positions.size) { index ->
                LowResolutionPosition(positions[index])
            }

    public fun getLowResolutionPosition(index: Int): LowResolutionPosition {
        return LowResolutionPosition(positions[index])
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PlayerInfoInitBlock

        if (localPlayerCoord != other.localPlayerCoord) return false
        if (!positions.contentEquals(other.positions)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = localPlayerCoord.hashCode()
        result = 31 * result + positions.contentHashCode()
        return result
    }

    override fun toString(): String {
        return "PlayerInfoInitBlock(" +
            "localPlayerCoord=$localPlayerCoord, " +
            "lowResolutionPositions=${lowResolutionPositions.contentToString()}" +
            ")"
    }
}
