package net.rsprox.protocol.rs3.common

public data class TypedVariable(
    public val id: Int,
    public val scriptType: Int,
    public val value: Value,
) {
    public sealed interface Value

    public data class IntValue(
        public val value: Int,
    ) : Value

    public data class LongValue(
        public val value: Long,
    ) : Value

    public data class StringValue(
        public val value: String,
    ) : Value

    public data class Coordinate(
        public val plane: Int,
        public val x: Int,
        public val y: Int,
        public val z: Int,
    ) : Value
}
