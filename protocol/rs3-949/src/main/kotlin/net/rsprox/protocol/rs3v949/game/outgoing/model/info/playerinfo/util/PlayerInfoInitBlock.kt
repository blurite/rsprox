package net.rsprox.protocol.rs3v949.game.outgoing.model.info.playerinfo.util

public class PlayerInfoInitBlock(
    public val localPlayerIndex: Int,
    public val localPlayerLevel: Int,
    public val localPlayerX: Int,
    public val localPlayerZ: Int,
    private val positions: IntArray,
) {
    public fun getLowResolutionPosition(index: Int): Int = positions[index]

    public val nonZeroPositionCount: Int
        get() = positions.count { it != 0 }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PlayerInfoInitBlock

        if (localPlayerIndex != other.localPlayerIndex) return false
        if (localPlayerLevel != other.localPlayerLevel) return false
        if (localPlayerX != other.localPlayerX) return false
        if (localPlayerZ != other.localPlayerZ) return false
        if (!positions.contentEquals(other.positions)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = localPlayerIndex
        result = 31 * result + localPlayerLevel
        result = 31 * result + localPlayerX
        result = 31 * result + localPlayerZ
        result = 31 * result + positions.contentHashCode()
        return result
    }

    override fun toString(): String {
        return "PlayerInfoInitBlock(" +
            "localPlayerIndex=$localPlayerIndex, " +
            "localPlayerLevel=$localPlayerLevel, localPlayerX=$localPlayerX, localPlayerZ=$localPlayerZ, " +
            "nonZeroPositionCount=${positions.count { it != 0 }}" +
            ")"
    }
}
