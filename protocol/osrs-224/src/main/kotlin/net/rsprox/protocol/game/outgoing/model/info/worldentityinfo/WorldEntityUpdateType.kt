package net.rsprox.protocol.game.outgoing.model.info.worldentityinfo

import net.rsprox.protocol.common.CoordGrid

public sealed interface WorldEntityUpdateType {
    public data object Idle : WorldEntityUpdateType

    public class LowResolutionToHighResolution(
        public val sizeX: Int,
        public val sizeZ: Int,
        public val angle: Int,
        public val unknownProperty: Int,
        public val coordGrid: CoordGrid,
    ) : WorldEntityUpdateType

    public data object HighResolutionToLowResolution : WorldEntityUpdateType

    public class Active(
        public val angle: Int,
        public val coordGrid: CoordGrid,
        public val moveSpeed: WorldEntityMoveSpeed,
    ) : WorldEntityUpdateType
}
