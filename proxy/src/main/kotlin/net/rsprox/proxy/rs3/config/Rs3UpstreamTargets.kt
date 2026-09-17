package net.rsprox.proxy.rs3.config

public data class Rs3UpstreamTargets(
    public val lobbyId: Int,
    public val lobbyHost: String,
    public val gamePort: Int,
    public val revision: Int,
)
