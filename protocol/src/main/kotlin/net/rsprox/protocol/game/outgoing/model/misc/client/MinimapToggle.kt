package net.rsprox.protocol.game.outgoing.model.misc.client

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

/**
 * Minimap toggle is used to modify the state of the minimap
 * and the attached compass.
 *
 * Minimap states table:
 * ```
 * | Id |           Description           |
 * |----|:-------------------------------:|
 * | 0  |             Enabled             |
 * | 1  |       Minimap unclickable       |
 * | 2  |          Minimap hidden         |
 * | 3  |          Compass hidden         |
 * | 4  | Map unclickable, compass hidden |
 * | 5  |             Disabled            |
 * ```
 *
 * @property minimapState the minimap state to set (see table above)
 */
public class MinimapToggle(
    public val minimapState: Int,
) : IncomingServerGameMessage {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MinimapToggle

        return minimapState == other.minimapState
    }

    override fun hashCode(): Int {
        return minimapState
    }

    override fun toString(): String {
        return "MinimapToggle(minimapState=$minimapState)"
    }
}
