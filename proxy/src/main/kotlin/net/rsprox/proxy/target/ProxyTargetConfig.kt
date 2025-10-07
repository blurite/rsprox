package net.rsprox.proxy.target

import net.rsprox.proxy.config.HTTP_SERVER_PORT

public data class ProxyTargetConfig(
    public val id: Int,
    public val name: String,
    public val javConfigUrl: String,
    public val modulus: String?,
    public val varpCount: Int,
    public val revision: String?,
    public val runeliteBootstrapCommitHash: String?,
    public val runeliteGamepackUrl: String?,
    public val binaryFolder: String?,
) {
    public val httpPort: Int
        get() = HTTP_SERVER_PORT + id
}
