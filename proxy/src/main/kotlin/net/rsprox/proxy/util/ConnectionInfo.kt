package net.rsprox.proxy.util

import java.math.BigInteger

public data class ConnectionInfo(
    public val clientType: ClientType,
    public val operatingSystem: OperatingSystem,
    public val port: Int,
    public val modulus: BigInteger,
)
