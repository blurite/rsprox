package net.rsprox.protocol.game.incoming.model.misc.user

import net.rsprot.protocol.ClientProtCategory
import net.rsprot.protocol.message.IncomingGameMessage
import net.rsprox.protocol.common.CoordGrid
import net.rsprox.protocol.game.incoming.GameClientProtCategory

/**
 * Click world map events are transmitted when the user double-clicks
 * on the world map. If the user has J-Mod privileges and holds the
 * 'Control' and 'Shift' keys down as they do the click, a different
 * packet is transmitted instead.
 * This packet is intended for a feature that never released - world
 * map hints. In the pre-eoc days, players could double-click on their
 * world map to set a 'Destination marker' which had a blue arrow to it,
 * allowing them easier navigation to the given destination.
 * In OldSchool RuneScape, there is a RuneLite plugin that accomplishes
 * the same thing. Additionally, the double-clicking is fairly broken
 * in the C++ client, and only sends this packet in some extreme cases
 * when dragging the world map around, not through the traditional
 * double-clicking.
 * @property x the absolute x coordinate to set the destination to
 * @property z the absolute z coordinate to set the destination to
 * @property level the level to set the destination to
 */
public class ClickWorldMap(
    private val coordGrid: CoordGrid,
) : IncomingGameMessage {
    public val x: Int
        get() = coordGrid.x
    public val z: Int
        get() = coordGrid.z
    public val level: Int
        get() = coordGrid.level
    override val category: ClientProtCategory
        get() = GameClientProtCategory.USER_EVENT

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ClickWorldMap

        return coordGrid == other.coordGrid
    }

    override fun hashCode(): Int {
        return coordGrid.hashCode()
    }

    override fun toString(): String {
        return "ClickWorldMap(" +
            "x=$x, " +
            "z=$z, " +
            "level=$level" +
            ")"
    }
}
