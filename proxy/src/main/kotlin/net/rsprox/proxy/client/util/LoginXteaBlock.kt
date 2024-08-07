package net.rsprox.proxy.client.util

import io.netty.buffer.ByteBuf

@Suppress("DuplicatedCode")
public data class LoginXteaBlock(
    public val username: String,
    public val packedClientSettings: Int,
    public val width: Int,
    public val height: Int,
    public val uuid: ByteArray,
    public val siteSettings: String,
    public val affiliate: Int,
    public val constZero: Int,
    public val hostPlatformStats: HostPlatformStats,
    public val secondClientType: Int,
    public val crcBlockHeader: Int,
    public val crc: ByteBuf,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LoginXteaBlock

        if (username != other.username) return false
        if (packedClientSettings != other.packedClientSettings) return false
        if (width != other.width) return false
        if (height != other.height) return false
        if (!uuid.contentEquals(other.uuid)) return false
        if (siteSettings != other.siteSettings) return false
        if (affiliate != other.affiliate) return false
        if (constZero != other.constZero) return false
        if (hostPlatformStats != other.hostPlatformStats) return false
        if (secondClientType != other.secondClientType) return false
        if (crcBlockHeader != other.crcBlockHeader) return false
        if (crc != other.crc) return false

        return true
    }

    override fun hashCode(): Int {
        var result = username.hashCode()
        result = 31 * result + packedClientSettings
        result = 31 * result + width
        result = 31 * result + height
        result = 31 * result + uuid.contentHashCode()
        result = 31 * result + siteSettings.hashCode()
        result = 31 * result + affiliate
        result = 31 * result + constZero
        result = 31 * result + hostPlatformStats.hashCode()
        result = 31 * result + secondClientType
        result = 31 * result + crcBlockHeader
        result = 31 * result + crc.hashCode()
        return result
    }

    override fun toString(): String {
        return "LoginXteaBlock(" +
            "username='$username', " +
            "packedClientSettings=$packedClientSettings, " +
            "width=$width, " +
            "height=$height, " +
            "uuid=${uuid.contentToString()}, " +
            "siteSettings='$siteSettings', " +
            "affiliate=$affiliate, " +
            "constZero=$constZero, " +
            "hostPlatformStats=$hostPlatformStats, " +
            "secondClientType=$secondClientType, " +
            "crcBlockHeader=$crcBlockHeader, " +
            "crc=$crc" +
            ")"
    }
}
