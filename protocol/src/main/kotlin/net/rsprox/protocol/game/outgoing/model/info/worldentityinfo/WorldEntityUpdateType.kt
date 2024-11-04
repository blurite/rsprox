package net.rsprox.protocol.game.outgoing.model.info.worldentityinfo

import net.rsprox.protocol.common.CoordFine
import net.rsprox.protocol.common.CoordGrid

public sealed interface WorldEntityUpdateType {
    public data object Idle : WorldEntityUpdateType

    public class LowResolutionToHighResolutionV2(
        public val sizeX: Int,
        public val sizeZ: Int,
        public val angle: Int,
        public val coordFine: CoordFine,
        public val level: Int,
    ) : WorldEntityUpdateType

    public class LowResolutionToHighResolutionV1(
        public val sizeX: Int,
        public val sizeZ: Int,
        public val angle: Int,
        public val unknownProperty: Int,
        public val coordGrid: CoordGrid,
    ) : WorldEntityUpdateType

    public data object HighResolutionToLowResolution : WorldEntityUpdateType

    public class ActiveV1(
        public val angle: Int,
        public val coordGrid: CoordGrid,
        public val moveSpeed: WorldEntityMoveSpeed,
    ) : WorldEntityUpdateType

    public class ActiveV2(
        public val angle: Int,
        public val coordFine: CoordFine,
        public val teleport: Boolean,
    ) : WorldEntityUpdateType
}
