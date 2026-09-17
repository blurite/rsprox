package net.rsprox.proxy.rs3.relay

import net.rsprox.proxy.worlds.LocalAddressRanges
import java.net.Inet4Address
import java.net.InetAddress

/**
 * Uses the OSRS world-ID packing with disjoint suffixes for RS3 worlds and lobbies.
 * [targetId] is an RS3-only target slot, independent of OSRS target IDs.
 * Constructing this value does not allocate a namespace or bind any sockets.
 */
public class Rs3LocalAddressSpace(
    public val targetId: Int,
) {
    init {
        require(targetId in 0 until LocalAddressRanges.RS3_TARGET_COUNT) { "Target id out of bounds: $targetId" }
    }

    public fun address(endpoint: Rs3Endpoint): Inet4Address {
        val suffix =
            when (endpoint) {
                is Rs3Endpoint.Lobby -> LocalAddressRanges.RS3_LOBBY_BASE + targetId
                is Rs3Endpoint.World -> LocalAddressRanges.RS3_WORLD_BASE + targetId
            }
        val octets = byteArrayOf(127, (endpoint.id ushr 8).toByte(), endpoint.id.toByte(), suffix.toByte())
        return InetAddress.getByAddress(octets) as Inet4Address
    }

    /** Returns null for other targets, ordinary localhost, IPv6 and non-loopback addresses. */
    public fun endpoint(address: InetAddress): Rs3Endpoint? {
        if (address !is Inet4Address) return null
        val bytes = address.address
        if (bytes[0] != 127.toByte()) return null
        val second = bytes[1].toInt() and 0xFF
        val third = bytes[2].toInt() and 0xFF
        val fourth = bytes[3].toInt() and 0xFF
        val id = (second shl 8) or third
        return when (fourth) {
            LocalAddressRanges.RS3_LOBBY_BASE + targetId -> Rs3Endpoint.Lobby(id)
            LocalAddressRanges.RS3_WORLD_BASE + targetId -> Rs3Endpoint.World(id)
            else -> null
        }
    }
}
