package net.rsprox.protocol.rs3.game.outgoing.model.info.playerinfo

import net.rsprox.protocol.rs3.game.outgoing.model.info.playerinfo.extendedinfo.PlayerExtendedInfo

public sealed interface PlayerUpdateType {
    public data class HighResolutionIdle(
        public val extendedInfo: List<PlayerExtendedInfo>,
    ) : PlayerUpdateType

    public data class LowResolutionToHighResolution(
        public val level: Int,
        public val x: Int,
        public val z: Int,
        public val movementMode: Int,
        public val extendedInfo: List<PlayerExtendedInfo>,
    ) : PlayerUpdateType

    public data class HighResolutionMovement(
        public val teleport: Boolean,
        public val level: Int,
        public val x: Int,
        public val z: Int,
        public val movementMode: Int,
        public val steps: List<Step>,
        public val extendedInfo: List<PlayerExtendedInfo>,
    ) : PlayerUpdateType

    public data class Step(
        public val x: Int,
        public val z: Int,
    )

    public data class LowResolutionMovement(
        public val level: Int,
        public val chunkX: Int,
        public val chunkZ: Int,
        public val movementMode: Int,
    ) : PlayerUpdateType

    public data class HighResolutionToLowResolution(
        public val level: Int,
        public val chunkX: Int,
        public val chunkZ: Int,
    ) : PlayerUpdateType
}
