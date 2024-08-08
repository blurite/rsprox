package net.rsprox.proxy.binary.credentials

public data class BinaryCredentials(
    public val displayName: String,
    public val userId: Long,
    public val userHash: Long,
)
