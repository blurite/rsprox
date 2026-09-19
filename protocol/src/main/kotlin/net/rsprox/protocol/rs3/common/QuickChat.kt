package net.rsprox.protocol.rs3.common

public data class QuickChat(
    public val template: String,
    public val parameters: List<Parameter>,
) {
    /** Null value denotes a server-computed command with no bytes in the client packet. */
    public data class Parameter(
        public val command: Int,
        public val arguments: List<Int>,
        public val value: Long?,
    )
}
