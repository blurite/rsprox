package net.rsprox.protocol.rs3.game.outgoing.model.map

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public data class EnvironmentOverride(
    public val flags: Long,
    public val fields: List<Field>,
    public val duration: Int,
) : IncomingServerGameMessage {
    /** Flag indexes are proven identities; values precede native colour/axis/boolean conversions. */
    public sealed interface Field {
        public val bit: Int
    }

    public data class IntegerValue(
        override val bit: Int,
        public val value: Int,
    ) : Field

    public data class WordValue(
        override val bit: Int,
        public val value: Int,
    ) : Field

    public data class ScalarValue(
        override val bit: Int,
        public val value: Float,
    ) : Field

    public data class VectorValue(
        override val bit: Int,
        public val x: Float,
        public val y: Float,
        public val z: Float,
    ) : Field

    public data class WordReserved(
        override val bit: Int,
        public val value: Int,
        public val reserved: List<Int>,
    ) : Field

    public data class WordScalar(
        override val bit: Int,
        public val value: Int,
        public val scalar: Float,
    ) : Field
}
