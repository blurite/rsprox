package net.rsprox.protocol.rs3.game.outgoing.model.info.npcinfo.extendedinfo

import net.rsprox.protocol.rs3.common.TypedVariable
import net.rsprox.protocol.rs3.game.outgoing.model.info.npcinfo.util.Rs3NpcUpdateMaskKey

/** Typed revision-independent values, in native mask and record order. */
public sealed interface NpcMask : NpcExtendedInfo {
    public val key: Rs3NpcUpdateMaskKey

    /** Scalar transport fields whose semantic names have not all been independently established. */
    public data class Scalars(
        override val key: Rs3NpcUpdateMaskKey,
        public val values: List<Int>,
    ) : NpcMask

    public data class Text(
        override val key: Rs3NpcUpdateMaskKey,
        public val text: String,
    ) : NpcMask

    public data class Transformation(
        public val id: Int,
    ) : NpcMask {
        override val key: Rs3NpcUpdateMaskKey = Rs3NpcUpdateMaskKey.TRANSFORMATION
    }

    public data class FaceEntity(
        public val target: Int,
    ) : NpcMask {
        override val key: Rs3NpcUpdateMaskKey = Rs3NpcUpdateMaskKey.FACE_ENTITY
    }

    public data class Sequence(
        public val ids: List<Int>,
        public val delay: Int,
    ) : NpcMask {
        override val key: Rs3NpcUpdateMaskKey = Rs3NpcUpdateMaskKey.SEQUENCE
    }

    public data class Variables(
        override val key: Rs3NpcUpdateMaskKey,
        public val discardedPrefix: Int,
        public val variables: List<TypedVariable>,
    ) : NpcMask

    public data class Stats(
        public val stats: List<Stat>,
    ) : NpcMask {
        override val key: Rs3NpcUpdateMaskKey = Rs3NpcUpdateMaskKey.NPC_STATS
    }

    public data class Stat(
        public val slot: Int,
        public val value: Int,
        public val auxiliary: Int,
    )

    public data class SlotPairs(
        public val slots: List<SlotPair>,
    ) : NpcMask {
        override val key: Rs3NpcUpdateMaskKey = Rs3NpcUpdateMaskKey.TINTING_CHANNELS
    }

    public data class SlotPair(
        public val slot: Int,
        public val id: Int,
        public val index: Int,
    )

    public data class Customisation(
        override val key: Rs3NpcUpdateMaskKey,
        public val flags: Int,
        public val models: List<Model>,
        public val recolours: List<Int>,
        public val retextures: List<Int>,
        public val colours: List<Int>,
    ) : NpcMask

    public data class Model(
        public val id: Int,
        public val scale: Float?,
        public val translation: List<Int>,
        public val rotation: List<Int>,
        public val values32: List<Int>,
        public val values64: List<Int>,
    )

    public data class Spotanims(
        public val removals: List<Int>,
        public val additions: List<Spotanim>,
    ) : NpcMask {
        override val key: Rs3NpcUpdateMaskKey = Rs3NpcUpdateMaskKey.SPOTANIM
    }

    public data class Spotanim(
        public val slot: Int,
        public val id: Int,
        public val packedHeightDelay: Int,
        public val rotationFlags: Int,
        public val packedOffsets: Int,
    )

    public data class Hits(
        public val wide: Boolean,
        public val hits: List<Hit>,
        public val headbars: List<Headbar>,
    ) : NpcMask {
        override val key: Rs3NpcUpdateMaskKey =
            if (wide) Rs3NpcUpdateMaskKey.HITMARKS_AND_HEADBARS_WIDE else Rs3NpcUpdateMaskKey.HITMARKS_AND_HEADBARS
    }

    public data class Hit(
        public val id: Int,
        public val value: Int,
        public val secondaryId: Int,
        public val secondaryValue: Int,
        public val delay: Int,
    )

    public data class Headbar(
        public val id: Int,
        public val duration: Int,
        public val delay: Int?,
        public val first: Int?,
        public val second: Int?,
        public val extraId: Int?,
        public val extraFirst: Int?,
        public val extraSecond: Int?,
    )

    public data class BoneTransforms(
        public val count: Int,
        public val transforms: List<BoneTransform>,
    ) : NpcMask {
        override val key: Rs3NpcUpdateMaskKey = Rs3NpcUpdateMaskKey.BONE_TRANSFORMS
    }

    public data class BoneTransform(
        public val flags: Int,
        public val slot: Int,
        public val id: Int?,
        public val translation: List<Int?>,
        public val rotation: List<Int?>,
        public val scale: List<Int?>,
    )
}
