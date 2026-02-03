package net.rsprox.protocol.game.outgoing.model.misc.player

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage
import net.rsprox.protocol.game.outgoing.model.zone.payload.util.CoordInBuildArea

/**
 * Set map flag is used to set the red map flag on the minimap.
 * Use values 255, 255 to remove the map flag.
 * @property xInBuildArea the x coordinate within the build area
 * to render the map flag at.
 * @property zInBuildArea the z coordinate within the build area
 * to render the map flag at.
 */
public class SetMapFlagV1 private constructor(
    private val coordInBuildArea: CoordInBuildArea,
) : IncomingServerGameMessage {
    public constructor(
        xInBuildArea: Int,
        zInBuildArea: Int,
    ) : this(
        CoordInBuildArea(xInBuildArea, zInBuildArea),
    )

    public val xInBuildArea: Int
        get() = coordInBuildArea.xInBuildArea
    public val zInBuildArea: Int
        get() = coordInBuildArea.zInBuildArea

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SetMapFlagV1

        return coordInBuildArea == other.coordInBuildArea
    }

    override fun hashCode(): Int {
        return coordInBuildArea.hashCode()
    }

    override fun toString(): String {
        return "SetMapFlag(" +
            "xInBuildArea=$xInBuildArea, " +
            "zInBuildArea=$zInBuildArea" +
            ")"
    }
}
