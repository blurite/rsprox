package net.rsprox.patch.native.processors.utils

@JvmInline
@OptIn(ExperimentalStdlibApi::class)
public value class HexBytePattern(
    private val pattern: String,
) {
    init {
        require(pattern.length and 0x1 == 0) {
            "Pattern $pattern does not follow the format of two characters per byte."
        }
        for (i in 0..<length) {
            // The getByteOrWildcard() function will throw an exception if the format is invalid
            getByteOrWildcard(i)
        }
    }

    public val length: Int
        get() = pattern.length / 2

    public fun getByteOrWildcard(index: Int): Byte? {
        val substring = pattern.substring(index * 2, (index + 1) * 2)
        return if (substring == "??") {
            null
        } else {
            substring.hexToByte(HexFormat.UpperCase)
        }
    }

    public infix fun isCompatible(other: HexBytePattern): Boolean {
        if (length != other.length) {
            return false
        }
        for (i in 0..<length) {
            val a = getByteOrWildcard(i)
            val b = other.getByteOrWildcard(i)
            if ((a == null && b == null) || (a != null && b != null)) {
                continue
            }
            return false
        }
        return true
    }

    override fun toString(): String {
        return "HexBytePattern(pattern='$pattern')"
    }
}
