package net.rsprox.protocol.game.outgoing.model.map

import net.rsprot.crypto.xtea.XteaKey

/**
 * Rebuild normal is sent when the game requires a map reload without being in instances.
 * @property zoneX the x coordinate of the local player's current zone.
 * @property zoneZ the z coordinate of the local player's current zone.
 * @property worldArea the current world area in which the player resides.
 * @property keys the list of xtea keys needed to decrypt the map.
 */
public class RebuildNormal private constructor(
    private val _zoneX: UShort,
    private val _zoneZ: UShort,
    private val _worldArea: UShort,
    override val keys: List<XteaKey>,
) : StaticRebuildMessage {
    public constructor(
        zoneX: Int,
        zoneZ: Int,
        worldArea: Int,
        keys: List<XteaKey>,
    ) : this(
        zoneX.toUShort(),
        zoneZ.toUShort(),
        worldArea.toUShort(),
        keys,
    )

    override val zoneX: Int
        get() = _zoneX.toInt()
    override val zoneZ: Int
        get() = _zoneZ.toInt()
    override val worldArea: Int
        get() = _worldArea.toInt()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RebuildNormal

        if (_zoneX != other._zoneX) return false
        if (_zoneZ != other._zoneZ) return false
        if (_worldArea != other._worldArea) return false
        if (keys != other.keys) return false

        return true
    }

    override fun hashCode(): Int {
        var result = _zoneX.hashCode()
        result = 31 * result + _zoneZ.hashCode()
        result = 31 * result + _worldArea.hashCode()
        result = 31 * result + keys.hashCode()
        return result
    }

    override fun toString(): String {
        return "RebuildNormal(" +
            "zoneX=$zoneX, " +
            "zoneZ=$zoneZ, " +
            "worldArea=$worldArea, " +
            "keys=$keys" +
            ")"
    }
}
