package net.rsprox.protocol.game.outgoing.model.misc.player

import net.rsprox.protocol.common.CoordGrid
import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

/**
 * Set map flag is used to set the red map flag on the minimap. Note that the [CoordGrid.level] property
 * is unused in this packet.
 * Use the no-arguments constructor to clear the map flag, or refer to [RESET] in the companion.
 * @property coordGrid the coord grid to show the map flag at.
 */
public class SetMapFlagV2(
    public val coordGrid: CoordGrid,
) : IncomingServerGameMessage {
    public constructor(
        x: Int,
        z: Int,
    ) : this(
        CoordGrid(0, x, z),
    )

    public constructor() : this(CoordGrid.INVALID)

    public val x: Int
        get() = coordGrid.x
    public val z: Int
        get() = coordGrid.z

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SetMapFlagV2

        return coordGrid == other.coordGrid
    }

    override fun hashCode(): Int {
        return coordGrid.hashCode()
    }

    override fun toString(): String {
        return "SetMapFlagV2(" +
            "x=$x, " +
            "z=$z" +
            ")"
    }

    public companion object {
        @JvmStatic
        public val RESET: SetMapFlagV2 = SetMapFlagV2()
    }
}
