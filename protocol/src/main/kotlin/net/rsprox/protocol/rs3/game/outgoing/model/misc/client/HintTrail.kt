package net.rsprox.protocol.rs3.game.outgoing.model.misc.client

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public data class HintTrail(
    public val slot: Int,
    public val modelId: Int,
    public val trail: Trail?,
) : IncomingServerGameMessage {
    public data class Trail(
        public val count: Int,
        public val baseX: Int,
        public val baseZ: Int,
        public val points: List<Point>,
    )

    public data class Point(
        public val deltaX: Int,
        public val deltaZ: Int,
    )
}
