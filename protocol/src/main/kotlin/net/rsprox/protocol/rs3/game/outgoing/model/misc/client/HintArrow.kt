package net.rsprox.protocol.rs3.game.outgoing.model.misc.client

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public data class HintArrow(
    public val slot: Int,
    public val kind: Int,
    public val payload: Payload,
) : IncomingServerGameMessage {
    public sealed interface Payload

    public data class Clear(
        public val reserved: List<Int>,
    ) : Payload

    public data class Actor(
        public val sprite: Int,
        public val index: Int,
        public val parameter: Int,
        public val reserved: List<Int>,
        public val parameter32: Int,
    ) : Payload

    public data class Location(
        public val sprite: Int,
        public val level: Int,
        public val x: Int,
        public val z: Int,
        public val height: Int,
        public val range: Int,
        public val parameter32: Int,
    ) : Payload

    public data class Other(
        public val sprite: Int,
        public val parameter32: Int,
        public val reserved: List<Int>,
    ) : Payload
}
