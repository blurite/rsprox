package net.rsprox.proxy.target

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
    public val gameServerPort: Int = DEFAULT_GAME_SERVER_PORT,
) {
    public companion object {
        public const val DEFAULT_GAME_SERVER_PORT: Int = 43594
    }
}
