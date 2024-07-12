package net.rsprox.protocol.game.outgoing.model.info.playerinfo.extendedinfo

public class SpotanimExtendedInfo(
    public val spotanims: Map<Int, Spotanim>,
) : ExtendedInfo {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SpotanimExtendedInfo

        return spotanims == other.spotanims
    }

    override fun hashCode(): Int {
        return spotanims.hashCode()
    }

    override fun toString(): String {
        return "SpotanimExtendedInfo(spotanims=$spotanims)"
    }
}
