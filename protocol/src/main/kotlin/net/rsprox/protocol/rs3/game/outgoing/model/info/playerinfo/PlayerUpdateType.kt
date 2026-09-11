package net.rsprox.protocol.rs3.game.outgoing.model.info.playerinfo

public sealed interface PlayerUpdateType {
    public data object LowResolutionIdle : PlayerUpdateType

    public data class HighResolutionIdle(
        public val name: String?,
        public val extendedInfo: List<String>,
    ) : PlayerUpdateType

    public data class LowResolutionToHighResolution(
        public val level: Int,
        public val x: Int,
        public val z: Int,
        public val name: String?,
        public val extendedInfo: List<String>,
    ) : PlayerUpdateType

    public data class HighResolutionMovement(
        public val teleport: Boolean,
        public val ambiguous: Boolean,
        public val level: Int?,
        public val x: Int?,
        public val z: Int?,
        public val rawX: Int?,
        public val rawZ: Int?,
        public val name: String?,
        public val extendedInfo: List<String>,
    ) : PlayerUpdateType

    public data class LowResolutionMovement(
        public val level: Int,
        public val chunkX: Int,
        public val chunkZ: Int,
    ) : PlayerUpdateType

    public data class HighResolutionToLowResolution(
        public val level: Int,
        public val chunkX: Int,
        public val chunkZ: Int,
    ) : PlayerUpdateType
}
