package net.rsprox.protocol.game.outgoing.model.map

import net.rsprot.crypto.xtea.XteaKey
import net.rsprox.protocol.game.outgoing.model.info.playerinfo.util.PlayerInfoInitBlock

/**
 * Rebuild login is sent as part of the login procedure as the very first packet,
 * as this one contains information about everyone's low resolution position, allowing
 * the player information packet to be initialized properly.
 * @property zoneX the x coordinate of the local player's current zone.
 * @property zoneZ the z coordinate of the local player's current zone.
 * @property worldArea the current world area in which the player resides.
 * @property keys the list of xtea keys needed to decrypt the map.
 * @property playerInfoInitBlock the initialization block of player info, containing the
 * absolute coordinate of the local player, and low resolution positions of everyone else.
 */
public class RebuildLogin private constructor(
    private val _zoneX: UShort,
    private val _zoneZ: UShort,
    private val _worldArea: UShort,
    override val keys: List<XteaKey>,
    public val playerInfoInitBlock: PlayerInfoInitBlock,
) : StaticRebuildMessage {
    public constructor(
        zoneX: Int,
        zoneZ: Int,
        worldArea: Int,
        keys: List<XteaKey>,
        playerInfoInitBlock: PlayerInfoInitBlock,
    ) : this(
        zoneX.toUShort(),
        zoneZ.toUShort(),
        worldArea.toUShort(),
        keys,
        playerInfoInitBlock,
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

        other as RebuildLogin

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
        return "RebuildLogin(" +
            "zoneX=$zoneX, " +
            "zoneZ=$zoneZ, " +
            "worldArea=$worldArea, " +
            "keys=$keys, " +
            "playerInfoInitBlock=$playerInfoInitBlock" +
            ")"
    }
}
