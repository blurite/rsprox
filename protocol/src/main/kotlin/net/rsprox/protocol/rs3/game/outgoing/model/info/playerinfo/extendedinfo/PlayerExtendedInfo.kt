package net.rsprox.protocol.rs3.game.outgoing.model.info.playerinfo.extendedinfo

/** Decoded wire values, independent of revision and presentation. */
public sealed interface PlayerExtendedInfo {
    /** An explicitly failed/deferred inner profile; the deobfuscated envelope has a proven length. */
    public data class UndecodedAppearance(
        public val payload: ByteArray,
        public val reason: String = "cache definitions required",
    ) : PlayerExtendedInfo {
        override fun equals(other: Any?): Boolean =
            other is UndecodedAppearance && payload.contentEquals(other.payload) && reason == other.reason

        override fun hashCode(): Int = 31 * payload.contentHashCode() + reason.hashCode()
    }

    public data class Appearance(
        public val flags: Int,
        public val size: Int,
        public val title: Int?,
        public val icons: List<NameIcon>,
        /** Signed state: native map exclusion tests != 0, scene exclusion tests == 1. */
        public val visibility: Int,
        public val npc: Int?,
        public val npcTeam: Int?,
        public val equipment: List<Equipment>,
        public val customisations: List<Customisation>,
        public val primaryColours: List<Int>,
        public val secondaryColours: List<Int>,
        public val renderAnimationSet: Int,
        public val name: String,
        public val combatLevel: Int,
        /** Null means this field was not transmitted; -1 suppresses the level label. */
        public val skillLevel: Int?,
        /** The full effective level, not the bonus added to combatLevel. */
        public val effectiveCombatLevel: Int?,
        /** Gates combat-difference colouring; not itself a combat-level difference. */
        public val combatColourParameter: Int?,
        /** Null is the zero-range wire form, which clears previous background sounds. */
        public val backgroundSound: BackgroundSound?,
    ) : PlayerExtendedInfo {
        public val gender: Int
            get() = flags and 1

        /** Selects one of two title enums; their authored gender labels are not yet verified. */
        public val titleVariant: Int
            get() = flags ushr 7 and 1
    }

    public data class NameIcon(
        public val sprite: Int,
        public val flags: Int,
    ) {
        public val menu: Boolean
            get() = flags and 1 != 0

        public val chatbox: Boolean
            get() = flags and 2 != 0
    }

    public data class BackgroundSound(
        /** Signed native range in tiles; the positional sound engine multiplies it by 512. */
        public val range: Int,
        public val stationary: Int,
        public val crawl: Int,
        public val walk: Int,
        public val run: Int,
        public val volume: Int,
    )

    public data class Equipment(
        public val slot: Int,
        public val kind: Kind,
        public val id: Int,
    ) {
        public enum class Kind {
            EMPTY,
            KIT,
            ITEM,
        }
    }

    public data class Customisation(
        public val slot: Int,
        public val flags: Int,
        public val bodyModels: List<ModelPair>,
        public val headModels: List<ModelPair>,
        public val recolours: List<PaletteReplacement>,
        public val retextures: List<PaletteReplacement>,
    )

    public data class ModelPair(
        public val slot: Int,
        public val male: Int,
        public val female: Int,
    )

    public data class PaletteReplacement(
        public val index: Int,
        public val value: Int,
    )

    public data class Hits(
        public val wide: Boolean,
        public val hits: List<Hit>,
        public val headbars: List<Headbar>,
    ) : PlayerExtendedInfo

    public data class Hit(
        public val type: Int,
        public val value: Int,
        public val secondaryType: Int,
        public val secondaryValue: Int,
        public val delay: Int,
    )

    public sealed interface Headbar {
        public val type: Int

        public data class Remove(
            override val type: Int,
        ) : Headbar

        /** Fills use 0..255; the optional secondary bar overlays the primary with shared timing. */
        public data class Update(
            override val type: Int,
            public val duration: Int,
            public val delay: Int,
            public val startFill: Int,
            public val endFill: Int,
            public val secondary: SecondaryHeadbar?,
        ) : Headbar
    }

    public data class SecondaryHeadbar(
        public val id: Int,
        public val startFill: Int,
        public val endFill: Int,
    )

    public data class Variables(
        public val full: Boolean,
        public val entries: List<Variable>,
    ) : PlayerExtendedInfo

    public data class Variable(
        public val id: Int,
        public val value: VariableValue,
    )

    public sealed interface VariableValue {
        public data class IntegerValue(
            public val value: Int,
        ) : VariableValue

        public data class LongValue(
            public val value: Long,
        ) : VariableValue

        public data class StringValue(
            public val value: String,
        ) : VariableValue

        /** Native coordinate axes are signed integers on the wire, converted to floats by the client. */
        public data class Coordinate(
            public val level: Int,
            public val x: Int,
            public val y: Int,
            public val z: Int,
        ) : VariableValue
    }

    /** Listed slots are retained; omitted slots are removed. Zero count releases all attachments. */
    public data class Attachments(
        /** Signed wire count; negative counts contain no entries but do not release the controller. */
        public val count: Int,
        public val attachments: List<Attachment>,
    ) : PlayerExtendedInfo

    public data class Attachment(
        public val flags: Int,
        public val slot: Int,
        /** OBJ for flag 0x400 (takes precedence), VFX for 0x800; preserve the full wire ID. */
        public val id: Int?,
        /** Model-space offsets; flag 0x40 rotates this offset using the supplied rotation. */
        public val translation: TransformAxes,
        /** Raw angle units, wrapped to 14 bits by native. Absent axes default to zero. */
        public val rotation: TransformAxes,
        /** Raw fixed-point multipliers; 10000 is unit scale. Absent axes default to unit scale. */
        public val scale: TransformAxes,
    )

    /** Present axes retain their exact integer wire units; absent axes are not invented wire values. */
    public data class TransformAxes(
        public val x: Int?,
        public val y: Int?,
        public val z: Int?,
    )

    public data class Sequence(
        public val ids: List<Int>,
        public val delay: Int,
    ) : PlayerExtendedInfo

    public data class ClanMember(
        public val enabled: Boolean,
    ) : PlayerExtendedInfo

    public data class FaceEntity(
        public val target: Int,
    ) : PlayerExtendedInfo

    /** Native consumes these fields without applying them. Retain the full payload for inspection. */
    public data class Unused(
        public val kind: Kind,
        public val field0: Int,
        public val field1: Int,
        public val field2: Int,
    ) : PlayerExtendedInfo {
        public enum class Kind {
            UNUSED_MASK_8,
            UNUSED_MASK_20,
            UNUSED_MASK_12,
            UNUSED_MASK_24,
            UNUSED_MASK_2,
            UNUSED_MASK_13,
        }
    }

    public data class SayV2(
        public val text: String,
        public val chatbox: Boolean,
    ) : PlayerExtendedInfo

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
    ) : PlayerExtendedInfo

    public data class Tinting(
        public val hue: Int,
        public val saturation: Int,
        public val lightness: Int,
        public val weight: Int,
        public val start: Int,
        public val end: Int,
    ) : PlayerExtendedInfo

    /** Revision 950 consumes all four fields without applying them. */
    public data class UnusedMask16(
        public val field0: Int,
        public val field1: Int,
        public val field2: Int,
        public val field3: Int,
    ) : PlayerExtendedInfo

    /** Minimap/headbar classification, not alpha. Retain unknown status values. */
    public data class PlayerStatus(
        public val value: Int,
    ) : PlayerExtendedInfo

    public data class SayV1(
        public val text: String,
    ) : PlayerExtendedInfo

    /** Raw wire angle; revision 950 uses 16,384 units per turn for whole-player facing. */
    public data class FaceAngle(
        public val angle: Int,
    ) : PlayerExtendedInfo

    public data class Spotanims(
        public val removals: List<Int>,
        public val additions: List<Spotanim>,
    ) : PlayerExtendedInfo

    public data class Spotanim(
        public val slot: Int,
        public val id: Int,
        public val packedHeightDelay: Int,
        public val rotationFlags: Int,
        public val packedOffsets: Int,
    )

    /** An update replaces the eight-slot set; omitted icons are cleared or faded out. */
    public data class HeadIcons(
        /** False only for an empty envelope, which native skips without changing any icons. */
        public val update: Boolean,
        public val slots: List<HeadIcon>,
    ) : PlayerExtendedInfo

    public data class HeadIcon(
        public val slot: Int,
        public val spriteIndex: Int,
        /** Sprite group, or -1 for no resource. */
        public val id: Int,
        /** All bits participate in change detection; only bits 0/1 control fading. */
        public val flags: Int,
        /** Milliseconds, not client cycles. Duration-only changes do not restart an existing icon. */
        public val fadeInDuration: Int,
        public val fadeOutDuration: Int,
        /** Changing this token retriggers the update even if the sprite and flags are unchanged. */
        public val restartKey: Int,
    ) {
        public val fadeIn: Boolean
            get() = flags and 1 != 0

        public val fadeOut: Boolean
            get() = flags and 2 != 0
    }
}
