package net.rsprox.proxy.rs3

/**
 * RSProx's decoder/recording key. The upper half identifies an obfuscation variant;
 * it is not Jagex's build subrevision and must not be sent to login or JS5.
 */
@JvmInline
public value class Rs3ProtocolRevision(
    public val value: Int,
) {
    init {
        require(value != -1 && value and 0xffff != 0) { "Invalid RS3 protocol revision key: $value" }
    }

    public val wireRevision: Int
        get() = value and 0xffff

    public val obfuscationVersion: Int
        get() = value ushr 16

    public fun display(subRevision: Int): String {
        val base = "$wireRevision.$subRevision"
        return if (obfuscationVersion == 0) base else "$base (beta $obfuscationVersion)"
    }

    public companion object {
        public const val LIVE_950: Int = 950
        public const val BETA_950_1: Int = 950 or (1 shl 16)

        public fun of(
            wireRevision: Int,
            obfuscationVersion: Int = 0,
        ): Rs3ProtocolRevision {
            require(wireRevision in 1..0xffff) { "RS3 wire revision exceeds 16 bits: $wireRevision" }
            require(obfuscationVersion in 0..0xffff) {
                "RS3 obfuscation version exceeds 16 bits: $obfuscationVersion"
            }
            return Rs3ProtocolRevision(wireRevision or (obfuscationVersion shl 16))
        }
    }
}
