package net.rsprox.proxy.rs3.relay

import io.netty.util.NetUtil
import java.net.InetAddress
import java.net.InetSocketAddress

/**
 * An immutable route to one real destination/port. Register both ports separately when
 * the native endpoint advertises an alternate port. Advertise only after binding succeeds.
 */
public class Rs3RelayRoute(
    public val addressSpace: Rs3LocalAddressSpace,
    public val endpoint: Rs3Endpoint,
    public val upstreamHost: String,
    public val localPort: Int,
    public val upstreamPort: Int,
) {
    public val localAddress: InetSocketAddress = InetSocketAddress(addressSpace.address(endpoint), localPort)

    init {
        require(localPort in 1024..65535 && localPort != 43594) { "Invalid RS3 local relay port: $localPort" }
        require(upstreamPort in 1..65535) { "Invalid upstream port: $upstreamPort" }
        require(upstreamHost.isNotBlank() && upstreamHost.none { it.isWhitespace() || it == '\u0000' }) {
            "Invalid upstream hostname"
        }
        require(!upstreamHost.trimEnd('.').equals("localhost", ignoreCase = true)) {
            "A relay route must point to the real upstream, not localhost"
        }
        val literal = NetUtil.createByteArrayFromIpAddressString(upstreamHost)
        if (literal != null) {
            val address = InetAddress.getByAddress(literal)
            require(!address.isLoopbackAddress && !address.isAnyLocalAddress) {
                "A relay route must not point back into local listeners"
            }
        }
    }
}
