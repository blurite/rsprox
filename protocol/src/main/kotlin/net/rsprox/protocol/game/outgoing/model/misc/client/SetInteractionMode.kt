package net.rsprox.protocol.game.outgoing.model.misc.client

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

/**
 * Sets the interaction mode for a specific world.
 *
 * Tile interaction modes table:
 *
 * ```md
 * | Id |   Type   |
 * |:--:|:--------:|
 * |  0 | Disabled |
 * |  1 |   Walk   |
 * |  2 |  Heading |
 * ```
 *
 * Entity interaction modes table:
 *
 * ```md
 * | Id |     Type     |
 * |:--:|:------------:|
 * |  0 |   Disabled   |
 * |  1 |    Enabled   |
 * |  2 | Examine Only |
 * ```
 *
 * @property worldId the id of the world to modify. If the value is -2, the default
 * behaviour for all worlds is changed.
 * @property tileInteractionMode sets the tile interaction mode. See the table above.
 * @property entityInteractionMode sets the entity interaction mode. See the table above.
 */
public class SetInteractionMode private constructor(
    private val _worldId: Short,
    private val _tileInteractionMode: UByte,
    private val _entityInteractionMode: UByte,
) : IncomingServerGameMessage {
    public constructor(
        worldId: Int,
        tileInteractionMode: Int,
        entityInteractionMode: Int,
    ) : this(
        worldId.toShort(),
        tileInteractionMode.toUByte(),
        entityInteractionMode.toUByte(),
    )

    public val worldId: Int
        get() = _worldId.toInt()
    public val tileInteractionMode: Int
        get() = _tileInteractionMode.toInt()
    public val entityInteractionMode: Int
        get() = _entityInteractionMode.toInt()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SetInteractionMode) return false

        if (_worldId != other._worldId) return false
        if (_tileInteractionMode != other._tileInteractionMode) return false
        if (_entityInteractionMode != other._entityInteractionMode) return false

        return true
    }

    override fun hashCode(): Int {
        var result = _worldId.toInt()
        result = 31 * result + _tileInteractionMode.hashCode()
        result = 31 * result + _entityInteractionMode.hashCode()
        return result
    }

    override fun toString(): String {
        return "SetInteractionMode(" +
            "worldId=$worldId, " +
            "tileInteractionMode=$tileInteractionMode, " +
            "entityInteractionMode=$entityInteractionMode" +
            ")"
    }
}
