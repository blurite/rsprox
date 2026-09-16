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
        public val icons: List<AppearanceIcon>,
        public val bodyType: Int,
        public val npc: Int?,
        public val npcTeam: Int?,
        public val equipment: List<Equipment>,
        public val customisations: List<Customisation>,
        public val primaryColours: List<Int>,
        public val secondaryColours: List<Int>,
        public val renderAnimationSet: Int,
        public val name: String,
        public val combatLevel: Int,
        public val totalLevel: Int?,
        public val visibleCombatLevel: Int?,
        public val combatDifference: Int?,
        public val extraFlag: Int,
        public val extra: List<Int>,
    ) : PlayerExtendedInfo

    public data class AppearanceIcon(public val id: Int, public val value: Int)

    public data class Equipment(public val slot: Int, public val kind: Kind, public val id: Int) {
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

    public data class ModelPair(public val slot: Int, public val male: Int, public val female: Int)

    public data class PaletteReplacement(public val index: Int, public val value: Int)

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

        public data class Remove(override val type: Int) : Headbar

        public data class Update(
            override val type: Int,
            public val duration: Int,
            public val delay: Int,
            public val startFill: Int,
            public val endFill: Int,
            public val extra: HeadbarExtra?,
        ) : Headbar
    }

    public data class HeadbarExtra(
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
        public data class IntegerValue(public val value: Int) : VariableValue

        public data class LongValue(public val value: Long) : VariableValue

        public data class StringValue(public val value: String) : VariableValue

        /** Native coordinate axes are signed integers on the wire, converted to floats by the client. */
        public data class Coordinate(
            public val level: Int,
            public val x: Int,
            public val y: Int,
            public val z: Int,
        ) : VariableValue
    }

    public data class BoneTransforms(
        // Zero releases transforms. A negative count retains an empty slot list instead.
        public val count: Int,
        public val transforms: List<BoneTransform>,
    ) : PlayerExtendedInfo

    public data class BoneTransform(
        public val flags: Int,
        public val slot: Int,
        public val id: Int?,
        public val translation: TransformAxes,
        public val rotation: TransformAxes,
        public val scale: TransformAxes,
    )

    /** Present axes retain their exact integer wire units; absent axes are not invented wire values. */
    public data class TransformAxes(
        public val x: Int?,
        public val y: Int?,
        public val z: Int?,
    )

    public data class Sequence(
        public val animations: List<Int>,
        public val delay: Int,
    ) : PlayerExtendedInfo

    /** Native boolean used by actor priority/display decisions; not a movement-speed enum. */
    public data class PriorityFlag(public val value: Int) : PlayerExtendedInfo {
        public val enabled: Boolean
            get() = value == 1
    }

    public data class FaceEntity(public val target: Int) : PlayerExtendedInfo

    public data class EnabledOps(
        public val field0: Int,
        public val field1: Int,
        public val field2: Int,
    ) : PlayerExtendedInfo

    // Native transport is proven; the meanings of all timed-effect variants are not yet established.
    public data class TimedEffect(
        public val kind: Kind,
        public val field0: Int,
        public val field1: Int,
        public val field2: Int,
    ) : PlayerExtendedInfo {
        public enum class Kind {
            SECONDARY_FREEZE,
            PLAYER_FREEZE,
            TIMED_EFFECT_1,
            TIMED_EFFECT_2,
            TIMED_EFFECT_3,
        }
    }

    public data class ForwardedChat(
        public val message: String,
        public val flags: Int,
    ) : PlayerExtendedInfo

    public data class ExactMove(
        public val field0: Int,
        public val field1: Int,
        public val field2: Int,
        public val field3: Int,
        public val field4: Int,
        public val field5: Int,
        public val field6: Int,
        public val field7: Int,
        public val field8: Int,
    ) : PlayerExtendedInfo

    public data class Tinting(
        public val field0: Int,
        public val field1: Int,
        public val field2: Int,
        public val field3: Int,
        public val field4: Int,
        public val field5: Int,
    ) : PlayerExtendedInfo

    public data class ScaleChange(
        public val field0: Int,
        public val field1: Int,
        public val field2: Int,
        public val field3: Int,
    ) : PlayerExtendedInfo

    public data class Transparency(public val value: Int) : PlayerExtendedInfo

    public data class Say(public val text: String) : PlayerExtendedInfo

    public data class HeadTurn(public val angle: Int) : PlayerExtendedInfo

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

    public data class MotionSlots(public val slots: List<MotionSlot>) : PlayerExtendedInfo

    public data class MotionSlot(
        public val slot: Int,
        public val value: Int,
        public val id: Int,
        public val flags: Int,
        public val startDuration: Int,
        public val endDuration: Int,
        public val key: Int,
    )
}
