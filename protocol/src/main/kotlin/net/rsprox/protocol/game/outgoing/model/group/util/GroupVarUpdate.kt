package net.rsprox.protocol.game.outgoing.model.group.util

public class GroupVarUpdate<out T>(
    public val index: Int,
    public val packedGroupVar: Int,
    public val variable: GroupVariable<T>,
) {
    init {
        require(index in 0..255) {
            "Index must be in range of 0..255"
        }
    }

    public val id: Int
        get() = packedGroupVar ushr ID_BIT_COUNT
    public val isMember: Boolean
        get() = ((packedGroupVar ushr MEMBER_BIT_COUNT) and 0x1) != 0
    public val baseVarType: Int
        get() = (packedGroupVar ushr BASE_VAR_TYPE_BIT_COUNT) and 0x3
    public val varIndex: Int
        get() = packedGroupVar and 0xFFFF

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GroupVarUpdate<*>

        if (index != other.index) return false
        if (packedGroupVar != other.packedGroupVar) return false
        if (variable != other.variable) return false

        return true
    }

    override fun hashCode(): Int {
        var result = index
        result = 31 * result + packedGroupVar
        result = 31 * result + variable.hashCode()
        return result
    }

    override fun toString(): String {
        return "GroupVarUpdate(" +
            "index=$index, " +
            "id=$id, " +
            "isMember=$isMember, " +
            "baseVarType=$baseVarType, " +
            "varIndex=$varIndex, " +
            "variable=$variable" +
            ")"
    }

    public companion object {
        public const val INT_BASE_VAR_TYPE: Int = 0
        public const val LONG_BASE_VAR_TYPE: Int = 1
        public const val STRING_BASE_VAR_TYPE: Int = 2

        private const val ID_BIT_COUNT: Int = 20
        private const val MEMBER_BIT_COUNT: Int = 18
        private const val BASE_VAR_TYPE_BIT_COUNT: Int = 16
    }
}