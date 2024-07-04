package net.rsprox.proxy.worlds

@JvmInline
public value class LocalHostAddress private constructor(
    public val ip: Int,
) {
    public constructor(ip: String) : this(ipStringToInt(ip)) {
        require(ip.startsWith("$LOCALHOST_GROUP_HEADER.")) {
            "Invalid localhost $ip"
        }
    }

    public override fun toString(): String {
        return "${ip and 0xFF}.${ip ushr 8 and 0xFF}.${ip ushr 16 and 0xFF}.${ip ushr 24 and 0xFF}"
    }

    public companion object {
        private const val LOCALHOST_GROUP_HEADER: Int = 127
        private const val LOCALHOST_GROUP_SUFFIX: Int = 2
        private val ipv4Regex =
            Regex("^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$")

        public fun fromWorldId(worldId: Int): LocalHostAddress {
            require(worldId in 0..65535) {
                "World id out of bounds: $worldId"
            }
            val b = worldId ushr 8
            val c = worldId and 0xFF
            return LocalHostAddress(
                bitpack(
                    LOCALHOST_GROUP_HEADER,
                    b,
                    c,
                    LOCALHOST_GROUP_SUFFIX,
                ),
            )
        }

        private fun ipStringToInt(ip: String): Int {
            require(ipv4Regex.matches(ip)) {
                "Invalid IPv4 address: $ip"
            }
            val (a, b, c, d) = ip.split('.').map(String::toInt)
            return bitpack(a, b, c, d)
        }

        private fun bitpack(
            a: Int,
            b: Int,
            c: Int,
            d: Int,
        ): Int {
            return a
                .or(b shl 8)
                .or(c shl 16)
                .or(d shl 24)
        }
    }
}
