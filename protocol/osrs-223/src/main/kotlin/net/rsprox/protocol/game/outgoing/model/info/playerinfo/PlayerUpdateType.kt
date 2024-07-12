package net.rsprox.protocol.game.outgoing.model.info.playerinfo

import net.rsprox.protocol.common.CoordGrid
import net.rsprox.protocol.game.outgoing.model.info.playerinfo.extendedinfo.ExtendedInfo
import net.rsprox.protocol.game.outgoing.model.info.playerinfo.util.LowResolutionPosition

public sealed interface PlayerUpdateType {
    public data object LowResolutionIdle : PlayerUpdateType

    public class HighResolutionIdle(
        public val extendedInfo: List<ExtendedInfo>,
    ) : PlayerUpdateType {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as HighResolutionIdle

            return extendedInfo == other.extendedInfo
        }

        override fun hashCode(): Int {
            return extendedInfo.hashCode()
        }

        override fun toString(): String {
            return "HighResolutionIdle(extendedInfo=$extendedInfo)"
        }
    }

    public class LowResolutionToHighResolution(
        public val coord: CoordGrid,
        public val extendedInfo: List<ExtendedInfo>,
    ) : PlayerUpdateType {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as LowResolutionToHighResolution

            if (coord != other.coord) return false
            if (extendedInfo != other.extendedInfo) return false

            return true
        }

        override fun hashCode(): Int {
            var result = coord.hashCode()
            result = 31 * result + extendedInfo.hashCode()
            return result
        }

        override fun toString(): String {
            return "LowResolutionToHighResolution(" +
                "coord=$coord, " +
                "extendedInfo=$extendedInfo" +
                ")"
        }
    }

    public class HighResolutionMovement(
        public val coord: CoordGrid,
        public val extendedInfo: List<ExtendedInfo>,
    ) : PlayerUpdateType {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as HighResolutionMovement

            if (coord != other.coord) return false
            if (extendedInfo != other.extendedInfo) return false

            return true
        }

        override fun hashCode(): Int {
            var result = coord.hashCode()
            result = 31 * result + extendedInfo.hashCode()
            return result
        }

        override fun toString(): String {
            return "HighResolutionMovement(" +
                "coord=$coord, " +
                "extendedInfo=$extendedInfo" +
                ")"
        }
    }

    public class LowResolutionMovement(
        public val lowResolutionPosition: LowResolutionPosition,
    ) : PlayerUpdateType {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as LowResolutionMovement

            return lowResolutionPosition == other.lowResolutionPosition
        }

        override fun hashCode(): Int {
            return lowResolutionPosition.hashCode()
        }

        override fun toString(): String {
            return "LowResolutionMovement(lowResolutionPosition=$lowResolutionPosition)"
        }
    }

    public class HighResolutionToLowResolution(
        public val lowResolutionPosition: LowResolutionPosition,
    ) : PlayerUpdateType {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as HighResolutionToLowResolution

            return lowResolutionPosition == other.lowResolutionPosition
        }

        override fun hashCode(): Int {
            return lowResolutionPosition.hashCode()
        }

        override fun toString(): String {
            return "HighResolutionToLowResolution(lowResolutionPosition=$lowResolutionPosition)"
        }
    }
}
