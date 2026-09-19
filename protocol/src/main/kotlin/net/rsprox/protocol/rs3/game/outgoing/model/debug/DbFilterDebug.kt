package net.rsprox.protocol.rs3.game.outgoing.model.debug

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public data class DbFilterDebug(
    public val version: Int,
    public val titleMarker: Int,
    public val title: String?,
    public val filter: Filter,
) : IncomingServerGameMessage {
    public sealed interface Value {
        public data class IntegerValue(
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

    public data class RowSet(
        public val version: Int,
        public val tableId: Int,
        public val rows: List<Int>?,
    )

    public sealed interface Filter {
        public data class CompareRows(
            public val rows: RowSet,
            public val version: Int,
            public val valueType: Int,
            public val value: Value,
            public val operator: Int,
        ) : Filter

        public data class Unary(
            public val version: Int,
            public val child: Filter,
        ) : Filter

        public data class Compound(
            public val type: Int,
            public val version: Int,
            public val first: List<Filter>,
            public val second: List<Filter>,
        ) : Filter

        public data class CompareField(
            public val version: Int,
            public val valueType: Int,
            public val fieldId: Int,
            public val operator: Int,
            public val operandVersion: Int,
            public val value: Value,
        ) : Filter

        public data class CompareFieldInteger(
            public val version: Int,
            public val valueType: Int,
            public val fieldId: Int,
            public val operator: Int,
            public val operandVersion: Int,
            public val value: Int,
        ) : Filter

        public data class ValueSet(
            public val rows: RowSet,
            public val version: Int,
            public val valueType: Int,
            public val values: List<Value>,
        ) : Filter

        public data class Text(
            public val version: Int,
            public val flags: Int,
            public val value: Int,
            public val textMarker: Int,
            public val text: String?,
        ) : Filter
    }
}
