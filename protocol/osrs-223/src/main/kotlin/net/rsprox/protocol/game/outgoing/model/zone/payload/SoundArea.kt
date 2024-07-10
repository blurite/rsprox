package net.rsprox.protocol.game.outgoing.model.zone.payload

import net.rsprot.protocol.ServerProtCategory
import net.rsprot.protocol.message.ZoneProt
import net.rsprox.protocol.common.OldSchoolZoneProt
import net.rsprox.protocol.game.outgoing.model.GameServerProtCategory
import net.rsprox.protocol.game.outgoing.model.zone.payload.util.CoordInZone

/**
 * Sound area packed is sent to play a sound effect at a specific coord.
 * Any players within [radius] tiles of the destination coord will
 * hear this sound effect played, if they have sound effects enabled.
 * The volume will change according to the player's distance to the
 * origin coord of the sound effect itself.
 * It is worth noting there is a maximum quantity of 50 area sound effects
 * that can play concurrently in the client across all the zones.
 * Therefore, a potential optimization one can do is prevent appending
 * any more area sound effects once the quantity has reached 50 in a zone.
 * @property id the id of the sound effect to play
 * @property delay the delay in client cycles (20ms/cc) until the
 * sound effect starts playing
 * @property loops how many loops the sound effect should do.
 * If the [loops] property is 0, the sound effect will not play.
 * @property radius the radius from the originating coord how far the sound
 * effect can be heard. Note that the client ignores the 4 higher bits of
 * this value, meaning the maximum radius is 31 tiles - anything above has
 * no effect.
 * @property size the size of the origin. In most cases, this should be
 * a value of 1. However, if a larger value is provided, it means the
 * client will treat the south-western coord provided here as the
 * south-western corner of the 'box' that is made with this size in mind,
 * for the purpose of having an evenly-spreading volume around this
 * element.
 * This size property is primarily used for larger NPCs, to make their
 * sound effects flow out smoothly from all sides.
 * @property xInZone the x coordinate of the sound effect within the zone it is in,
 * a value in range of 0 to 7 (inclusive) is expected. Any bits outside that are ignored.
 * @property zInZone the z coordinate of the sound effect within the zone it is in,
 * a value in range of 0 to 7 (inclusive) is expected. Any bits outside that are ignored.
 */
@Suppress("DuplicatedCode")
public class SoundArea private constructor(
    private val _id: UShort,
    private val _delay: UByte,
    private val _loops: UByte,
    private val _radius: UByte,
    private val _size: UByte,
    private val coordInZone: CoordInZone,
) : ZoneProt {
    public constructor(
        id: Int,
        delay: Int,
        loops: Int,
        radius: Int,
        size: Int,
        xInZone: Int,
        zInZone: Int,
    ) : this(
        id.toUShort(),
        delay.toUByte(),
        loops.toUByte(),
        radius.toUByte(),
        size.toUByte(),
        CoordInZone(xInZone, zInZone),
    )

    public val id: Int
        get() = _id.toInt()
    public val delay: Int
        get() = _delay.toInt()
    public val loops: Int
        get() = _loops.toInt()
    public val radius: Int
        get() = _radius.toInt()
    public val size: Int
        get() = _size.toInt()
    public val xInZone: Int
        get() = coordInZone.xInZone
    public val zInZone: Int
        get() = coordInZone.zInZone

    public val coordInZonePacked: Int
        get() = coordInZone.packed.toInt()
    override val category: ServerProtCategory
        get() = GameServerProtCategory.HIGH_PRIORITY_PROT
    override val protId: Int = OldSchoolZoneProt.SOUND_AREA

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SoundArea

        if (_id != other._id) return false
        if (_delay != other._delay) return false
        if (_loops != other._loops) return false
        if (_radius != other._radius) return false
        if (_size != other._size) return false
        if (coordInZone != other.coordInZone) return false

        return true
    }

    override fun hashCode(): Int {
        var result = _id.hashCode()
        result = 31 * result + _delay.hashCode()
        result = 31 * result + _loops.hashCode()
        result = 31 * result + _radius.hashCode()
        result = 31 * result + _size.hashCode()
        result = 31 * result + coordInZone.hashCode()
        return result
    }

    override fun toString(): String {
        return "SoundArea(" +
            "id=$id, " +
            "delay=$delay, " +
            "loops=$loops, " +
            "radius=$radius, " +
            "size=$size, " +
            "xInZone=$xInZone, " +
            "zInZone=$zInZone" +
            ")"
    }
}
