package net.rsprox.proxy.target

public data class ProxyTargetConfig(
    public val id: Int,
    public val name: String,
    public val javConfigUrl: String,
    public val httpPort: Int,
)
