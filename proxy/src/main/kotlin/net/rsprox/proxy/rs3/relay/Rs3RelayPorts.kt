package net.rsprox.proxy.rs3.relay

/** Client-facing ports are independent of the real server's primary and alternate ports. */
public data class Rs3RelayPorts(
    public val primary: Int,
    public val alternate: Int,
) {
    init {
        require(primary in 1024..65534 && alternate == primary + 1) { "Expected consecutive RS3 relay ports" }
        require(primary != UPSTREAM_PRIMARY && alternate != UPSTREAM_PRIMARY) { "Cannot use the upstream game port" }
    }

    public fun routes(
        addresses: Rs3LocalAddressSpace,
        endpoint: Rs3Endpoint,
        host: String,
        primary: Int = UPSTREAM_PRIMARY,
        alternate: Int = UPSTREAM_ALTERNATE,
    ): List<Rs3RelayRoute> =
        listOf(
            Rs3RelayRoute(addresses, endpoint, host, this.primary, primary),
            Rs3RelayRoute(addresses, endpoint, host, this.alternate, alternate),
        )

    public companion object {
        public const val UPSTREAM_PRIMARY: Int = 43594
        public const val UPSTREAM_ALTERNATE: Int = 443
    }
}
