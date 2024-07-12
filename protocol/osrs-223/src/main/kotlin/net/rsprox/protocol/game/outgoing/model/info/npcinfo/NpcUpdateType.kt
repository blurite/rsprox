package net.rsprox.protocol.game.outgoing.model.info.npcinfo

import net.rsprox.protocol.game.outgoing.model.info.shared.extendedinfo.ExtendedInfo

public sealed interface NpcUpdateType {
    public data object Idle : NpcUpdateType

    public class LowResolutionToHighResolution(
        public val spawnCycle: Int,
        public val x: Int,
        public val z: Int,
        public val level: Int,
        public val angle: Int,
        public val jump: Boolean,
        public val extendedInfo: List<ExtendedInfo>,
    ) : NpcUpdateType {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as LowResolutionToHighResolution

            if (spawnCycle != other.spawnCycle) return false
            if (x != other.x) return false
            if (z != other.z) return false
            if (level != other.level) return false
            if (angle != other.angle) return false
            if (jump != other.jump) return false
            if (extendedInfo != other.extendedInfo) return false

            return true
        }

        override fun hashCode(): Int {
            var result = spawnCycle
            result = 31 * result + x
            result = 31 * result + z
            result = 31 * result + level
            result = 31 * result + angle
            result = 31 * result + jump.hashCode()
            result = 31 * result + extendedInfo.hashCode()
            return result
        }

        override fun toString(): String {
            return "LowResolutionToHighResolution(" +
                "spawnCycle=$spawnCycle, " +
                "x=$x, " +
                "z=$z, " +
                "level=$level, " +
                "angle=$angle, " +
                "jump=$jump, " +
                "extendedInfo=$extendedInfo" +
                ")"
        }
    }

    public data object HighResolutionToLowResolution : NpcUpdateType

    public class Active(
        public val x: Int,
        public val z: Int,
        public val level: Int,
        public val steps: List<Int>,
        public val moveSpeed: MoveSpeed,
        public val extendedInfo: List<ExtendedInfo>,
    ) : NpcUpdateType {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Active

            if (x != other.x) return false
            if (z != other.z) return false
            if (level != other.level) return false
            if (steps != other.steps) return false
            if (moveSpeed != other.moveSpeed) return false
            if (extendedInfo != other.extendedInfo) return false

            return true
        }

        override fun hashCode(): Int {
            var result = x
            result = 31 * result + z
            result = 31 * result + level
            result = 31 * result + steps.hashCode()
            result = 31 * result + moveSpeed.hashCode()
            result = 31 * result + extendedInfo.hashCode()
            return result
        }

        override fun toString(): String {
            return "Active(" +
                "x=$x, " +
                "z=$z, " +
                "level=$level, " +
                "steps=$steps, " +
                "moveSpeed=$moveSpeed, " +
                "extendedInfo=$extendedInfo" +
                ")"
        }
    }
}
