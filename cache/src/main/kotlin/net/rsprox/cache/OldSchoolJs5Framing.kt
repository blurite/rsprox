package net.rsprox.cache

import io.netty.buffer.ByteBuf

public enum class OldSchoolJs5Framing(
    private val groupSize: Int,
) {
    LEGACY(2),
    WIDE(3),
    ;

    public val prefixSize: Int
        get() = 1 + groupSize

    public val headerSize: Int
        get() = prefixSize + 5

    public fun readGroup(input: ByteBuf): Int =
        if (this == WIDE) input.readUnsignedMedium() else input.readUnsignedShort()

    public fun writeGroup(output: ByteBuf, group: Int) {
        require(group in 0 until (1 shl (groupSize * 8))) { "Invalid JS5 group: $group" }
        if (this == WIDE) output.writeMedium(group) else output.writeShort(group)
    }

    /** Container offsets include the compression byte and length, but not the archive/group prefix. */
    public fun remainingInBlock(containerOffset: Int): Int {
        val first = 512 - prefixSize
        return if (containerOffset < first) first - containerOffset else 511 - (containerOffset - first) % 511
    }

    public fun wireSize(containerSize: Int): Int {
        val remaining = (containerSize.toLong() - remainingInBlock(0)).coerceAtLeast(0)
        return Math.toIntExact(prefixSize + containerSize.toLong() + (remaining + 510) / 511)
    }

    public companion object {
        public fun forMasterIndex(revision: Int, wireData: ByteArray): OldSchoolJs5Framing {
            // Archive 255, group 255 starts FF 00 FF on legacy connections and FF 00 00 FF on wide ones.
            if (wireData.size >= 3 &&
                wireData[0] == 0xFF.toByte() &&
                wireData[1] == 0.toByte() &&
                wireData[2] == 0xFF.toByte()
            ) {
                return LEGACY
            }
            return forRevision(revision)
        }

        public fun forRevision(revision: Int): OldSchoolJs5Framing = if (revision >= 243) WIDE else LEGACY
    }
}
