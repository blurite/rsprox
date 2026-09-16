package net.rsprox.protocol.rs3.game.outgoing.model.telemetry

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public data class TelemetryGridMoveColumn(
    public val source: Int,
    public val group: Int,
    public val destination: Int,
) : IncomingServerGameMessage
