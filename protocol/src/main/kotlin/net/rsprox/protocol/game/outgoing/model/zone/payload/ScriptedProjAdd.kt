package net.rsprox.protocol.game.outgoing.model.zone.payload

import net.rsprox.protocol.game.outgoing.model.zone.payload.util.CoordInZone
import net.rsprox.protocol.common.OldSchoolZoneProt
import net.rsprox.protocol.common.CoordGrid
import net.rsprox.protocol.game.outgoing.model.IncomingZoneProt

/**
 * Scripted projectiles are used to send projectiles which can be
 * controlled by the server and clientscripts, to alter their path mid-travel.
 *
 * @property slot the projectile entity slot, similar to how players, npcs
 * and world entities have their own slots (or indexing).
 * @property id the spotanim id of the projectile.
 * @property coordInZone the in-zone coordinate where the projectile begins
 * travelling.
 * @property sourceOffsetX an x-axis offset in fine client units to apply to the
 * starting coordinate. A value of -64 would put it on the western edge of
 * the tile, while a value of +64 would put it on the eastern one. Supports
 * large offsets.
 * @property sourceOffsetZ a z-axis offset in fine client units to apply to the
 * starting coordinate. A value of -64 would put it on the southern edge of
 * the tile, while a value of +64 would put it on the northern one. Supports
 * large offsets.
 * @property sourceHeight the starting height of the projectile.
 * @property sourceIndex the index of the pathing entity from whom the projectile comes.
 * If the value is 0, the projectile will not be locked to any source entity.
 *
 * If the source avatar is a player, set the value as `-(index + 1)`
 *
 * If the source avatar is a NPC, set the value as `(index + 1)`
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
 * @property startTime the start time in client cycles (20ms/cc) until the
 * projectile begins moving
 * @property endTime the end time in client cycles (20ms/cc) until the
 * projectile arrives at its destination
 * @property curveScriptH an interpolation clientscript to invoke to get the
 * horizontal offset perpendicular to the travel direction. The script is
 * supplied a progress variable between 0 and 65535, and should return the
 * offset value to apply at that stage of the projectile's lifetime.
 * @property curveScriptA an interpolation clientscript to invoke to get the
 * arc offset perpendicular to the travel direction (arc lies in the plane formed
 * by the y-axis and the travel direction; in simpler terms, vertical offset).
 * The script is supplied a progress variable between 0 and 65535,
 * and should return the offset value to apply at that stage of the projectile's
 * lifetime.
 * @property curveScriptT an interpolation clientscript to invoke to get the
 * travel progress. This effectively acts as an easing function between start
 * and end coordinate. The script is supplied a progress variable between 0 and 65535,
 * and should return the offset value to apply at that stage of the projectile's
 * lifetime.
 */
public class ScriptedProjAdd private constructor(
    private val _slot: UShort,
    private val _id: UShort,
    private val coordInZone: CoordInZone,
    private val _sourceOffsetX: Short,
    private val _sourceOffsetZ: Short,
    private val _sourceHeight: Short,
    public val sourceIndex: Int,
    public val targetCoord: CoordGrid,
    private val _targetOffsetX: Short,
    private val _targetOffsetZ: Short,
    private val _targetHeight: Short,
    public val targetIndex: Int,
    private val _startTime: UShort,
    private val _endTime: UShort,
    private val _curveScriptH: UShort,
    private val _curveScriptA: UShort,
    private val _curveScriptT: UShort,
) : IncomingZoneProt {
    public constructor(
        slot: Int,
        id: Int,
        xInZone: Int,
        zInZone: Int,
        sourceOffsetX: Int,
        sourceOffsetZ: Int,
        sourceHeight: Int,
        sourceIndex: Int,
        targetCoord: CoordGrid,
        targetOffsetX: Int,
        targetOffsetZ: Int,
        targetHeight: Int,
        targetIndex: Int,
        startTime: Int,
        endTime: Int,
        curveScriptH: Int,
        curveScriptA: Int,
        curveScriptT: Int,
    ) : this(
        slot.toUShort(),
        id.toUShort(),
        CoordInZone(xInZone, zInZone),
        sourceOffsetX.toShort(),
        sourceOffsetZ.toShort(),
        sourceHeight.toShort(),
        sourceIndex,
        targetCoord,
        targetOffsetX.toShort(),
        targetOffsetZ.toShort(),
        targetHeight.toShort(),
        targetIndex,
        startTime.toUShort(),
        endTime.toUShort(),
        curveScriptH.toUShort(),
        curveScriptA.toUShort(),
        curveScriptT.toUShort(),
    )
    public constructor(
        slot: Int,
        id: Int,
        xInZone: Int,
        zInZone: Int,
        sourceOffsetX: Int,
        sourceOffsetZ: Int,
        sourceHeight: Int,
        sourceIndex: Int,
        targetLevel: Int,
        targetX: Int,
        targetZ: Int,
        targetOffsetX: Int,
        targetOffsetZ: Int,
        targetHeight: Int,
        targetIndex: Int,
        startTime: Int,
        endTime: Int,
        curveScriptH: Int,
        curveScriptA: Int,
        curveScriptT: Int,
    ) : this(
        slot.toUShort(),
        id.toUShort(),
        CoordInZone(xInZone, zInZone),
        sourceOffsetX.toShort(),
        sourceOffsetZ.toShort(),
        sourceHeight.toShort(),
        sourceIndex,
        CoordGrid(targetLevel, targetX, targetZ),
        targetOffsetX.toShort(),
        targetOffsetZ.toShort(),
        targetHeight.toShort(),
        targetIndex,
        startTime.toUShort(),
        endTime.toUShort(),
        curveScriptH.toUShort(),
        curveScriptA.toUShort(),
        curveScriptT.toUShort(),
    )

    public val slot: Int
        get() = _slot.toInt()
    public val id: Int
        get() = _id.toInt()
    public val xInZone: Int
        get() = coordInZone.xInZone
    public val zInZone: Int
        get() = coordInZone.zInZone
    public val sourceOffsetX: Int
        get() = _sourceOffsetX.toInt()
    public val sourceOffsetZ: Int
        get() = _sourceOffsetZ.toInt()
    public val sourceHeight: Int
        get() = _sourceHeight.toInt()
    public val targetOffsetX: Int
        get() = _targetOffsetX.toInt()
    public val targetOffsetZ: Int
        get() = _targetOffsetZ.toInt()
    public val targetHeight: Int
        get() = _targetHeight.toInt()
    public val startTime: Int
        get() = _startTime.toInt()
    public val endTime: Int
        get() = _endTime.toInt()
    public val curveScriptH: Int
        get() = _curveScriptH.toInt()
    public val curveScriptA: Int
        get() = _curveScriptA.toInt()
    public val curveScriptT: Int
        get() = _curveScriptT.toInt()

    public val coordInZonePacked: Int
        get() = coordInZone.packed.toInt()
    override val protId: Int = OldSchoolZoneProt.SCRIPTEDPROJ_ADD

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ScriptedProjAdd

        if (_sourceOffsetX != other._sourceOffsetX) return false
        if (_sourceOffsetZ != other._sourceOffsetZ) return false
        if (_sourceHeight != other._sourceHeight) return false
        if (sourceIndex != other.sourceIndex) return false
        if (_targetOffsetX != other._targetOffsetX) return false
        if (_targetOffsetZ != other._targetOffsetZ) return false
        if (_targetHeight != other._targetHeight) return false
        if (targetIndex != other.targetIndex) return false
        if (protId != other.protId) return false
        if (_slot != other._slot) return false
        if (_id != other._id) return false
        if (coordInZone != other.coordInZone) return false
        if (targetCoord != other.targetCoord) return false
        if (_startTime != other._startTime) return false
        if (_endTime != other._endTime) return false
        if (_curveScriptH != other._curveScriptH) return false
        if (_curveScriptA != other._curveScriptA) return false
        if (_curveScriptT != other._curveScriptT) return false

        return true
    }

    override fun hashCode(): Int {
        var result = _sourceOffsetX.toInt()
        result = 31 * result + _sourceOffsetZ
        result = 31 * result + _sourceHeight
        result = 31 * result + sourceIndex
        result = 31 * result + _targetOffsetX
        result = 31 * result + _targetOffsetZ
        result = 31 * result + _targetHeight
        result = 31 * result + targetIndex
        result = 31 * result + protId
        result = 31 * result + _slot.hashCode()
        result = 31 * result + _id.hashCode()
        result = 31 * result + coordInZone.hashCode()
        result = 31 * result + targetCoord.hashCode()
        result = 31 * result + _startTime.hashCode()
        result = 31 * result + _endTime.hashCode()
        result = 31 * result + _curveScriptH.hashCode()
        result = 31 * result + _curveScriptA.hashCode()
        result = 31 * result + _curveScriptT.hashCode()
        return result
    }

    override fun toString(): String {
        return "ScriptedProjAdd(" +
            "slot=$slot, " +
            "id=$id, " +
            "coordInZone=$coordInZone, " +
            "sourceOffsetX=$sourceOffsetX, " +
            "sourceOffsetZ=$sourceOffsetZ, " +
            "sourceHeight=$sourceHeight, " +
            "sourceIndex=$sourceIndex, " +
            "targetCoord=$targetCoord, " +
            "targetOffsetX=$targetOffsetX, " +
            "targetOffsetZ=$targetOffsetZ, " +
            "targetHeight=$targetHeight, " +
            "targetIndex=$targetIndex, " +
            "startTime=$startTime, " +
            "endTime=$endTime, " +
            "curveScriptH=$curveScriptH, " +
            "curveScriptA=$curveScriptA, " +
            "curveScriptT=$curveScriptT" +
            ")"
    }
}
