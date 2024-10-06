package net.rsprox.shared.account

public data class JagexCharacter(
    public val accountId: Int,
    public val displayName: String?,
    public val userHash: Long,
) {
    val safeDisplayName: String
        get() = displayName ?: "No name set ($accountId)"
}
