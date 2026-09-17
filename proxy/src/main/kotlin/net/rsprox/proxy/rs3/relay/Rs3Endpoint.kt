package net.rsprox.proxy.rs3.relay

/** The connection's role is determined by its listening address, never by arrival order. */
public sealed interface Rs3Endpoint {
    public val id: Int

    public data class Lobby(
        override val id: Int,
    ) : Rs3Endpoint {
        init {
            require(id in 0..65535) { "Lobby id out of bounds: $id" }
        }
    }

    public data class World(
        override val id: Int,
    ) : Rs3Endpoint {
        init {
            require(id in 0..65535) { "World id out of bounds: $id" }
        }
    }
}
