package net.rsprox.protocol.game.outgoing.model.map

import net.rsprot.crypto.xtea.XteaKey
import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage
import net.rsprox.protocol.game.outgoing.model.info.playerinfo.util.PlayerInfoInitBlock
import net.rsprox.protocol.game.outgoing.model.map.util.BuildArea

/**
 * Rebuild worldentity packet is used to build a new world entity block,
 * which will be rendered in the root world for the player.
 * @property index the index of the world entity (0-2048)
 * @property baseX the absolute base x coordinate of the world entity in the instance land
 * @property baseZ the absolute base z coordinate of the world entity in the instance land
 * @property buildArea the list of zones that will be built into the root world
 * @property keys the xtea keys needed to decrypt the map in build area
 * @property playerInfoInitBlock the player info initialization block for the world entity
 */
public class RebuildWorldEntity(
    private val _index: UShort,
    private val _baseX: UShort,
    private val _baseZ: UShort,
    public val buildArea: BuildArea,
    public val keys: List<XteaKey>,
    public val playerInfoInitBlock: PlayerInfoInitBlock,
) : IncomingServerGameMessage {
    public constructor(
        index: Int,
        baseX: Int,
        baseZ: Int,
        buildArea: BuildArea,
        keys: List<XteaKey>,
        playerInfoInitBlock: PlayerInfoInitBlock,
    ) : this(
        index.toUShort(),
        baseX.toUShort(),
        baseZ.toUShort(),
        buildArea,
        keys,
        playerInfoInitBlock,
    )

    public val index: Int
        get() = _index.toInt()
    public val baseX: Int
        get() = _baseX.toInt()
    public val baseZ: Int
        get() = _baseZ.toInt()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RebuildWorldEntity

        if (_index != other._index) return false
        if (_baseX != other._baseX) return false
        if (_baseZ != other._baseZ) return false
        if (buildArea != other.buildArea) return false
        if (keys != other.keys) return false
        if (playerInfoInitBlock != other.playerInfoInitBlock) return false

        return true
    }

    override fun hashCode(): Int {
        var result = _index.hashCode()
        result = 31 * result + _baseX.hashCode()
        result = 31 * result + _baseZ.hashCode()
        result = 31 * result + buildArea.hashCode()
        result = 31 * result + keys.hashCode()
        result = 31 * result + playerInfoInitBlock.hashCode()
        return result
    }

    override fun toString(): String {
        return "RebuildWorldEntity(" +
            "index=$index, " +
            "baseX=$baseX, " +
            "baseZ=$baseZ, " +
            "buildArea=$buildArea, " +
            "keys=$keys, " +
            "playerInfoInitBlock=$playerInfoInitBlock" +
            ")"
    }
}
