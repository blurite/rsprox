package net.rsprox.protocol.rs3.game.outgoing.model.telemetry

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public data class TelemetryClearGridValue(
    public val group: Int,
    public val column: Int,
    public val row: Int,
) : IncomingServerGameMessage
