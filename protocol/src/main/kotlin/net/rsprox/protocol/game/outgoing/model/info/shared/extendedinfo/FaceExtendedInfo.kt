package net.rsprox.protocol.game.outgoing.model.info.shared.extendedinfo

public class FaceExtendedInfo(
    public val faceType: FaceType,
    public val walkType: WalkType,
    public val instant: Boolean,
) : ExtendedInfo {
    public sealed interface FaceType

    public data class EntityFaceType(
        public val entityType: EntityType,
        public val index: Int,
        public val fallbackAngle: Int,
    ) : FaceType

    public data class AngleFaceType(
        public val angle: Int,
    ) : FaceType

    public data class LocFaceType(
        public val x: Int,
        public val z: Int,
        public val sizeX: Int,
        public val sizeZ: Int,
    ) : FaceType

    public data object ResetFaceType : FaceType

    public enum class EntityType {
        None,
        Npc,
        Player,
        WorldEntity,
    }

    public enum class WalkType {
        CancelOnWalk,
        TurnOnWalk,
    }
}
