package net.rsprox.cache.util

public data class CacheGroupRequest(
    public val archive: Int,
    public val group: Int,
    public val urgent: Boolean = true,
)
