package net.rsprox.protocol.game.outgoing.model.misc.client

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

/**
 * Resets the interaction mode for a specific world.
 * @property worldId the id of the world to modify.
 */
public class ResetInteractionMode private constructor(
    private val _worldId: Short,
) : IncomingServerGameMessage {
    public constructor(worldId: Int) : this(worldId.toShort())

    public val worldId: Int
        get() = _worldId.toInt()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ResetInteractionMode) return false

        if (_worldId != other._worldId) return false

        return true
    }

    override fun hashCode(): Int {
        return _worldId.toInt()
    }

    override fun toString(): String {
        return "SetInteractionMode(" +
            "worldId=$worldId" +
            ")"
    }
}
