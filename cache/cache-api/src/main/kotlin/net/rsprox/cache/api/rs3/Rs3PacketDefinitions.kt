package net.rsprox.cache.api.rs3

/** Immutable metadata snapshot. Implementations must not perform I/O on packet-decoder threads. */
public interface Rs3PacketDefinitions : Rs3AppearanceDefinitions {
    public fun getNpc(id: Int): Rs3NpcDefinition

    public fun getVarbit(id: Int): Rs3VarbitDefinition

    public fun getQuickChatPhrase(id: Int): Rs3QuickChatPhrase

    public fun getVariable(
        domain: Rs3VariableDomain,
        id: Int,
    ): Rs3VariableDefinition
}

public enum class Rs3VariableDomain(
    public val group: Int,
) {
    PLAYER(60),
    CLIENT(62),
    CLAN(66),
    PLAYER_GROUP(80),
}

public data class Rs3NpcDefinition(
    public val recolourCount: Int,
    public val retextureCount: Int,
    public val morph: Rs3NpcMorph? = null,
)

public data class Rs3NpcMorph(
    public val varbit: Int,
    public val varp: Int,
    /** Last entry is the fallback, including -1 (invisible). */
    public val types: List<Int>,
)

public data class Rs3VarbitDefinition(
    public val domain: Int,
    public val base: Int,
    public val startBit: Int,
    public val endBit: Int,
)

public enum class Rs3BaseVarType {
    INT,
    LONG,
    STRING,
    COORDINATE,
}

public data class Rs3VariableDefinition(
    public val scriptType: Int,
    public val baseType: Rs3BaseVarType,
)

public data class Rs3QuickChatPhrase(
    public val template: String,
    public val commands: List<Rs3QuickChatCommand>,
)

public data class Rs3QuickChatCommand(
    public val id: Int,
    public val transmitBytes: Int,
    public val receiveBytes: Int,
    public val arguments: List<Int>,
)
