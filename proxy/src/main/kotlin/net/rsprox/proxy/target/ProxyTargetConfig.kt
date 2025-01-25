package net.rsprox.proxy.target

public data class ProxyTargetConfig(
    public val id: Int,
    public val name: String,
    public val javConfigUrl: String,
    public val httpPort: Int,
    public val modulus: String? = null,
    public val varpCount: Int = DEFAULT_VARP_COUNT,
) {
    public companion object {
        public const val DEFAULT_VARP_COUNT: Int = 5000
    }
}
