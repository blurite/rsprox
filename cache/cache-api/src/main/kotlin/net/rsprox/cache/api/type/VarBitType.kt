package net.rsprox.cache.api.type

@Suppress("SpellCheckingInspection")
public interface VarBitType {
    public val id: Int
    public val basevar: Int
    public val startbit: Int
    public val endbit: Int

    public fun bitmask(bitcount: Int): Int {
        return BITMASKS[bitcount]
    }

    private companion object {
        private val BITMASKS: IntArray = generateBitmasks()

        private fun generateBitmasks(): IntArray {
            var value = 2
            return IntArray(32) {
                val cur = value
                value += cur
                cur - 1
            }
        }
    }
}
