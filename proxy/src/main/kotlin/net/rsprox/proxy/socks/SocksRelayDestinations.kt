package net.rsprox.proxy.socks

import java.net.InetSocketAddress
import java.util.concurrent.ConcurrentHashMap

public data object SocksRelayDestinations {
    private val dictionary = ConcurrentHashMap<Int, InetSocketAddress>()

    public operator fun get(relayPort: Int): InetSocketAddress? {
        return dictionary[relayPort]
    }

    public operator fun set(
        relayPort: Int,
        destination: InetSocketAddress,
    ) {
        dictionary[relayPort] = destination
    }

    public fun remove(
        relayPort: Int,
        destination: InetSocketAddress,
    ) {
        dictionary.remove(relayPort, destination)
    }
}
