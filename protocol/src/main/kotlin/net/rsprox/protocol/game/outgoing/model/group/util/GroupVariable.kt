package net.rsprox.protocol.game.outgoing.model.group.util

public sealed interface GroupVariable<out T> {
    public val value: T

    public class IntGroupVariable(
        override val value: Int,
    ) : GroupVariable<Int> {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as IntGroupVariable

            return value == other.value
        }

        override fun hashCode(): Int {
            return value
        }

        override fun toString(): String {
            return "IntGroupVariable(" +
                "value=$value" +
                ")"
        }
    }

    public class LongGroupVariable(
        override val value: Long,
    ) : GroupVariable<Long> {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as LongGroupVariable

            return value == other.value
        }

        override fun hashCode(): Int {
            return value.hashCode()
        }

        override fun toString(): String {
            return "LongGroupVariable(" +
                "value=$value" +
                ")"
        }
    }

    public class StringGroupVariable(
        override val value: String,
    ) : GroupVariable<String> {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as StringGroupVariable

            return value == other.value
        }

        override fun hashCode(): Int {
            return value.hashCode()
        }

        override fun toString(): String {
            return "StringGroupVariable(" +
                "value='$value'" +
                ")"
        }
    }

    public class UnknownGroupVariable(
        public val baseVarType: Int,
        public val data: ByteArray,
    ) : GroupVariable<ByteArray> {
        override val value: ByteArray
            get() = data

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as UnknownGroupVariable

            if (baseVarType != other.baseVarType) return false
            return data.contentEquals(other.data)
        }

        override fun hashCode(): Int {
            var result = baseVarType
            result = 31 * result + data.contentHashCode()
            return result
        }

        override fun toString(): String {
            return "UnknownGroupVariable(" +
                "baseVarType=$baseVarType, " +
                "data=${data.contentToString()}" +
                ")"
        }
    }
}
