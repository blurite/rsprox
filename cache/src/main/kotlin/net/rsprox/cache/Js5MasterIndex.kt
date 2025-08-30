package net.rsprox.cache

import java.security.MessageDigest

@Suppress("MemberVisibilityCanBePrivate")
public class Js5MasterIndex(
    public val revision: Int,
    public val data: ByteArray,
) {
    @OptIn(ExperimentalStdlibApi::class)
    public fun shortHash(): String {
        val messageDigest = MessageDigest.getInstance("SHA-256")
        messageDigest.update(data)
        return messageDigest.digest().toHexString(0, 16, HexFormat.UpperCase)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Js5MasterIndex

        if (revision != other.revision) return false
        if (!data.contentEquals(other.data)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = revision
        result = 31 * result + data.contentHashCode()
        return result
    }

    override fun toString(): String {
        return "Js5MasterIndex(" +
            "revision=$revision, " +
            "data=${data.contentToString()}" +
            ")"
    }

    public companion object {
        public fun trimmed(
            revision: Int,
            data: ByteArray,
        ): Js5MasterIndex {
            val copyOfData = if (data.isEmpty()) data else data.copyOfRange(3, data.size)
            return Js5MasterIndex(
                revision,
                copyOfData,
            )
        }
    }
}
