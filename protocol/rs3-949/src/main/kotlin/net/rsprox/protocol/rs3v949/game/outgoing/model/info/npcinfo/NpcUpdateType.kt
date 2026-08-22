package net.rsprox.protocol.rs3v949.game.outgoing.model.info.npcinfo

import net.rsprox.protocol.rs3v949.game.outgoing.model.info.npcinfo.extendedinfo.NpcExtendedInfo


public sealed interface NpcUpdateType {
    public data object Idle : NpcUpdateType

    public enum class MovementType {
        WALK,
        RUN,
        STEP_ALT,
        EXT_ONLY,
    }

    public data class Active(
        public val movementType: MovementType,
        public val direction1: Int?,
        public val direction2: Int?,
        public val level: Int,
        public val x: Int,
        public val z: Int,
        public val extendedInfo: List<NpcExtendedInfo>,
    ) : NpcUpdateType

    public data class Add(
        public val id: Int,
        public val deltaX: Int,
        public val deltaZ: Int,
        public val level: Int,
        public val x: Int,
        public val z: Int,
        public val direction: Int,
        public val extendedInfo: List<NpcExtendedInfo>,
    ) : NpcUpdateType

    public data object Remove : NpcUpdateType
}
