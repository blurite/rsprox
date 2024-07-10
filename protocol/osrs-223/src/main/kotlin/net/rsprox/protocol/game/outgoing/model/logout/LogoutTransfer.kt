package net.rsprox.protocol.game.outgoing.model.logout

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

/**
 * Logout transfer packet is used for world-hopping purposes,
 * making the client connect to a different world instead.
 *
 * World properties table:
 * ```
 * | Flag       |           Type          |
 * |------------|:-----------------------:|
 * | 0x1        |         Members         |
 * | 0x2        |        Quick chat       |
 * | 0x4        |        PvP world        |
 * | 0x8        |        Lootshare        |
 * | 0x10       |    Dedicated activity   |
 * | 0x20       |       Bounty world      |
 * | 0x40       |        PvP Arena        |
 * | 0x80       | High level only - 1500+ |
 * | 0x100      |         Speedrun        |
 * | 0x200      |  Existing players only  |
 * | 0x400      |  Extra-hard wilderness  |
 * | 0x800      |      Dungeoneering      |
 * | 0x1000     |      Instance shard     |
 * | 0x2000     |         Rentable        |
 * | 0x4000     |    Last man standing    |
 * | 0x8000     |       New players       |
 * | 0x10000    |        Beta world       |
 * | 0x20000    |      Staff IP only      |
 * | 0x40000    | High level only - 2000+ |
 * | 0x80000    | High level only - 2400+ |
 * | 0x100000   |        VIPs only        |
 * | 0x200000   |       Hidden world      |
 * | 0x400000   |       Legacy only       |
 * | 0x800000   |         EoC only        |
 * | 0x1000000  |       Behind proxy      |
 * | 0x2000000  |       No save mode      |
 * | 0x4000000  |     Tournament world    |
 * | 0x8000000  |    Fresh start world    |
 * | 0x10000000 | High level only - 1750+ |
 * | 0x20000000 |      Deadman world      |
 * | 0x40000000 |      Seasonal world     |
 * | 0x80000000 |  External partner only  |
 * ```
 *
 * @property host the ip address of the new world
 * @property id the id of the new world
 * @property properties the flags of the new world
 */
public class LogoutTransfer private constructor(
    public val host: String,
    private val _id: UShort,
    public val properties: Int,
) : IncomingServerGameMessage {
    public constructor(
        host: String,
        id: Int,
        properties: Int,
    ) : this(
        host,
        id.toUShort(),
        properties,
    )

    public val id: Int
        get() = _id.toInt()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LogoutTransfer

        if (host != other.host) return false
        if (_id != other._id) return false
        if (properties != other.properties) return false

        return true
    }

    override fun hashCode(): Int {
        var result = host.hashCode()
        result = 31 * result + _id.hashCode()
        result = 31 * result + properties
        return result
    }

    override fun toString(): String {
        return "LogoutTransfer(" +
            "host='$host', " +
            "id=$id, " +
            "properties=$properties" +
            ")"
    }
}
