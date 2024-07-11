package net.rsprox.protocol.game.outgoing.model.specific

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage
import net.rsprox.protocol.game.outgoing.model.zone.payload.util.CoordInBuildArea
import net.rsprox.protocol.game.outgoing.model.zone.payload.util.LocProperties

/**
 * Loc anim specific packets are used to make a loc play an animation,
 * specific to one player and not the entire world.
 * @property id the id of the animation to play
 * @property zoneX the x coordinate of the zone's south-western corner in the
 * build area.
 * @property xInZone the x coordinate of the loc within the zone it is in,
 * a value in range of 0 to 7 (inclusive) is expected. Any bits outside that are ignored.
 * @property zoneZ the z coordinate of the zone's south-western corner in the
 * build area.
 * @property zInZone the z coordinate of the loc within the zone it is in,
 * a value in range of 0 to 7 (inclusive) is expected. Any bits outside that are ignored.
 * @property shape the shape of the loc, a value of 0 to 22 (inclusive) is expected.
 * @property rotation the rotation of the loc, a value of 0 to 3 (inclusive) is expected.
 *
 * It should be noted that the [zoneX] and [zoneZ] coordinates are relative
 * to the build area in their absolute form, not in their shifted zone form.
 * If the player is at an absolute coordinate of 50, 40 within the build area(104x104),
 * the expected coordinates to transmit here would be 48, 40, as that would
 * point to the south-western corner of the zone in which the player is standing in.
 * The client will add up the respective [zoneX] + [xInZone] properties together,
 * along with [zoneZ] + [zInZone] to re-create the effects of a normal zone packet.
 */
public class LocAnimSpecific private constructor(
    private val _id: UShort,
    private val coordInBuildArea: CoordInBuildArea,
    private val locProperties: LocProperties,
) : IncomingServerGameMessage {
    public constructor(
        id: Int,
        zoneX: Int,
        xInZone: Int,
        zoneZ: Int,
        zInZone: Int,
        shape: Int,
        rotation: Int,
    ) : this(
        id.toUShort(),
        CoordInBuildArea(
            zoneX,
            xInZone,
            zoneZ,
            zInZone,
        ),
        LocProperties(shape, rotation),
    )

    public constructor(
        id: Int,
        xInBuildArea: Int,
        zInBuildArea: Int,
        shape: Int,
        rotation: Int,
    ) : this(
        id.toUShort(),
        CoordInBuildArea(
            xInBuildArea,
            zInBuildArea,
        ),
        LocProperties(shape, rotation),
    )

    internal constructor(
        id: Int,
        coordInBuildArea: CoordInBuildArea,
        locProperties: LocProperties,
    ) : this(
        id.toUShort(),
        coordInBuildArea,
        locProperties,
    )

    public val id: Int
        get() = _id.toInt()
    public val zoneX: Int
        get() = coordInBuildArea.zoneX
    public val xInZone: Int
        get() = coordInBuildArea.xInZone
    public val zoneZ: Int
        get() = coordInBuildArea.zoneZ
    public val zInZone: Int
        get() = coordInBuildArea.zInZone
    public val shape: Int
        get() = locProperties.shape
    public val rotation: Int
        get() = locProperties.rotation

    public val coordInBuildAreaPacked: Int
        get() = coordInBuildArea.packedMedium
    public val locPropertiesPacked: Int
        get() = locProperties.packed.toInt()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LocAnimSpecific

        if (_id != other._id) return false
        if (coordInBuildArea != other.coordInBuildArea) return false
        if (locProperties != other.locProperties) return false

        return true
    }

    override fun hashCode(): Int {
        var result = _id.hashCode()
        result = 31 * result + coordInBuildArea.hashCode()
        result = 31 * result + locProperties.hashCode()
        return result
    }

    override fun toString(): String {
        return "LocAnimSpecific(" +
            "id=$id, " +
            "zoneX=$zoneX, " +
            "xInZone=$xInZone, " +
            "zoneZ=$zoneZ, " +
            "zInZone=$zInZone, " +
            "shape=$shape, " +
            "rotation=$rotation" +
            ")"
    }
}
