package net.rsprox.protocol.game.outgoing.model.zone.payload

import net.rsprox.protocol.common.OldSchoolZoneProt
import net.rsprox.protocol.common.CoordGrid
import net.rsprox.protocol.game.outgoing.model.IncomingZoneProt
import net.rsprot.protocol.message.toIntOrMinusOne

/**
 * Scripted projectile changes are used to alter the target of a scripted
 * projectile that's already in flight mid-flight. It can also be used
 * to freeze the projectile for a duration, then resume or delete afterwards.
 *
 * @property slot the projectile entity slot, similar to how players, npcs
 * and world entities have their own slots (or indexing).
 * @property targetCoord the absolute coordinate the projectile will fly to,
 * unless the path is modified afterwards via scriptedproj_change prot.
 * @property targetOffsetX an x-axis offset in fine client units to apply to the
 * destination coordinate. A value of -64 would put it on the western edge of
 * the tile, while a value of +64 would put it on the eastern one. Supports
 * large offsets.
 * @property targetOffsetZ a z-axis offset in fine client units to apply to the
 * destination coordinate. A value of -64 would put it on the southern edge of
 * the tile, while a value of +64 would put it on the northern one. Supports
 * large offsets.
 * @property targetHeight the end height of the projectile.
 * @property targetIndex the index of the pathing entity at whom the projectile is shot.
 * If the value is 0, the projectile will not be locked to any target entity.
 *
 * If the target avatar is a player, set the value as `-(index + 1)`
 *
 * If the target avatar is a NPC, set the value as `(index + 1)`
 * @property freezeDuration the number of client cycles (20ms/cc) to freeze the
 * projectile mid-flight for, before either resuming or deleting it. A value of
 * -1 can be used to indicate no freeze, which can then be coupled with
 * [deleteOnFreezeEnd] to immediately delete it mid-flight.
 * @property deleteOnFreezeEnd whether to delete the projectile when the
 * freeze ends.
 */
public class ScriptedProjChange private constructor(
    private val _slot: UShort,
    public val targetCoord: CoordGrid,
    private val _targetOffsetX: Short,
    private val _targetOffsetZ: Short,
    private val _targetHeight: Short,
    public val targetIndex: Int,
    private val _freezeDuration: UShort,
    public val deleteOnFreezeEnd: Boolean,
) : IncomingZoneProt {
    public constructor(
        slot: Int,
        targetCoord: CoordGrid,
        targetOffsetX: Int,
        targetOffsetZ: Int,
        targetHeight: Int,
        targetIndex: Int,
        freezeDuration: Int,
        deleteOnFreezeEnd: Boolean,
    ) : this(
        slot.toUShort(),
        targetCoord,
        targetOffsetX.toShort(),
        targetOffsetZ.toShort(),
        targetHeight.toShort(),
        targetIndex,
        freezeDuration.toUShort(),
        deleteOnFreezeEnd,
    )
    public constructor(
        slot: Int,
        targetLevel: Int,
        targetX: Int,
        targetZ: Int,
        targetOffsetX: Int,
        targetOffsetZ: Int,
        targetHeight: Int,
        targetIndex: Int,
        freezeDuration: Int,
        deleteOnFreezeEnd: Boolean,
    ) : this(
        slot.toUShort(),
        CoordGrid(targetLevel, targetX, targetZ),
        targetOffsetX.toShort(),
        targetOffsetZ.toShort(),
        targetHeight.toShort(),
        targetIndex,
        freezeDuration.toUShort(),
        deleteOnFreezeEnd,
    )

    public val slot: Int
        get() = _slot.toInt()
    public val targetOffsetX: Int
        get() = _targetOffsetX.toInt()
    public val targetOffsetZ: Int
        get() = _targetOffsetZ.toInt()
    public val targetHeight: Int
        get() = _targetHeight.toInt()
    public val freezeDuration: Int
        get() = _freezeDuration.toIntOrMinusOne()

    override val protId: Int = OldSchoolZoneProt.SCRIPTEDPROJ_CHANGE

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ScriptedProjChange

        if (_targetOffsetX != other._targetOffsetX) return false
        if (_targetOffsetZ != other._targetOffsetZ) return false
        if (_targetHeight != other._targetHeight) return false
        if (targetIndex != other.targetIndex) return false
        if (deleteOnFreezeEnd != other.deleteOnFreezeEnd) return false
        if (protId != other.protId) return false
        if (_slot != other._slot) return false
        if (targetCoord != other.targetCoord) return false
        if (_freezeDuration != other._freezeDuration) return false

        return true
    }

    override fun hashCode(): Int {
        var result = _targetOffsetX.toInt()
        result = 31 * result + _targetOffsetZ
        result = 31 * result + _targetHeight
        result = 31 * result + targetIndex
        result = 31 * result + deleteOnFreezeEnd.hashCode()
        result = 31 * result + protId
        result = 31 * result + _slot.hashCode()
        result = 31 * result + targetCoord.hashCode()
        result = 31 * result + _freezeDuration.hashCode()
        return result
    }

    override fun toString(): String {
        return "ScriptedProjChange(" +
            "slot=$slot, " +
            "targetCoord=$targetCoord, " +
            "targetOffsetX=$targetOffsetX, " +
            "targetOffsetZ=$targetOffsetZ, " +
            "targetHeight=$targetHeight, " +
            "targetIndex=$targetIndex, " +
            "freezeDuration=$freezeDuration, " +
            "deleteOnFreezeEnd=$deleteOnFreezeEnd" +
            ")"
    }
}
