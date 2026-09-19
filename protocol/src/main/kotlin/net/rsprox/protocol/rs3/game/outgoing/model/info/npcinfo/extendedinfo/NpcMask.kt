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

    /** Signed scene-priority override; null restores the NPC definition's priority offset. */
    public data class PriorityOffset(
        public val offset: Int?,
    ) : NpcMask {
        override val key: Rs3NpcUpdateMaskKey = Rs3NpcUpdateMaskKey.PRIORITY_OFFSET
    }

    /** Null removes the override and restores normal base-animation-set selection. */
    public data class BasOverride(
        public val bas: Int?,
    ) : NpcMask {
        override val key: Rs3NpcUpdateMaskKey = Rs3NpcUpdateMaskKey.BAS_OVERRIDE
    }

    /** Bypasses tile-overlap priority suppression, not other visibility checks. */
    public data class OverlapCulling(
        public val disabled: Boolean,
    ) : NpcMask {
        override val key: Rs3NpcUpdateMaskKey = Rs3NpcUpdateMaskKey.OVERLAP_CULLING
    }

    public data class FaceEntity(
        public val target: Int,
    ) : NpcMask {
        override val key: Rs3NpcUpdateMaskKey = Rs3NpcUpdateMaskKey.FACE_ENTITY
    }

    /** Tile and plane offsets from one movement base; delays are relative client cycles. */
    public data class ExactMove(
        public val deltaX1: Int,
        public val deltaZ1: Int,
        public val deltaX2: Int,
        public val deltaZ2: Int,
        public val deltaLevel1: Int,
        public val deltaLevel2: Int,
        public val delay1: Int,
        public val delay2: Int,
        public val angle: Int,
    ) : NpcMask {
        override val key: Rs3NpcUpdateMaskKey = Rs3NpcUpdateMaskKey.EXACT_MOVE
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
        public val currentLevel: Int,
        public val baseLevel: Int,
    )

    /** All eight wire slots, including omitted slots expanded to -1/-1 (empty). */
    public data class HeadIconCustomisation(
        public val slots: List<HeadIcon>,
    ) : NpcMask {
        override val key: Rs3NpcUpdateMaskKey = Rs3NpcUpdateMaskKey.HEADICON_CUSTOMISATION
    }

    public data class HeadIcon(
        public val slot: Int,
        public val group: Int,
        public val spriteIndex: Int,
    )

    public data class Customisation(
        override val key: Rs3NpcUpdateMaskKey,
        public val flags: Int,
        public val models: List<Model>,
        public val recolours: List<Int>,
        public val retextures: List<Int>,
        /** Ten palette selection indices, not packed colour values. */
        public val paletteIndices: List<Int>,
        /** Palette slots paired with the values above, not their packed wire indices. */
        public val recolourSlots: List<Int> = recolours.indices.toList(),
        public val retextureSlots: List<Int> = retextures.indices.toList(),
    ) : NpcMask

    public data class Model(
        public val id: Int,
        public val scale: Float?,
        public val rotation: List<Int>,
        /** Wire-space offsets; the native model builder negates Y. */
        public val translation: List<Int>,
        /** Consecutive signed source/destination pairs; -1 disables the pair. */
        public val recolours: List<Int>,
        public val retextures: List<Int>,
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
            if (wide) Rs3NpcUpdateMaskKey.HITMARKS_AND_HEADBARS_V2 else Rs3NpcUpdateMaskKey.HITMARKS_AND_HEADBARS_V1
    }

    public data class Hit(
        /** -1 is a non-rendered hit record; its accompanying wire value is still retained. */
        public val id: Int,
        public val value: Int,
        public val secondaryId: Int,
        public val secondaryValue: Int,
        public val delay: Int,
    )

    /** Fill values use 0..255. The optional secondary bar overlays the primary with shared timing. */
    public data class Headbar(
        public val id: Int,
        public val duration: Int,
        public val delay: Int?,
        public val startFill: Int?,
        public val endFill: Int?,
        public val secondaryId: Int?,
        public val secondaryStartFill: Int?,
        public val secondaryEndFill: Int?,
    )

    /** Listed slots are retained; omitted slots are removed. Zero count releases all attachments. */
    public data class Attachments(
        /** Signed wire count; negative counts contain no entries but do not release the controller. */
        public val count: Int,
        public val attachments: List<Attachment>,
    ) : NpcMask {
        override val key: Rs3NpcUpdateMaskKey = Rs3NpcUpdateMaskKey.ATTACHMENTS
    }

    public data class Attachment(
        public val flags: Int,
        public val slot: Int,
        /** OBJ for flag 0x400 (takes precedence), VFX for 0x800; preserve the full wire ID. */
        public val id: Int?,
        /** Model-space offsets; flag 0x40 rotates this offset using the supplied rotation. */
        public val translation: List<Int?>,
        /** Raw 14-bit angle units; 16384 is one turn. Absent axes default to zero. */
        public val rotation: List<Int?>,
        /** Raw fixed-point multipliers; 10000 is unit scale. Absent axes default to unit scale. */
        public val scale: List<Int?>,
    )
}
