package net.rsprox.protocol.rs3.game.outgoing.model.telemetry

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public data class TelemetryGridMoveRow(
    public val group: Int,
    public val source: Int,
    public val destination: Int,
) : IncomingServerGameMessage
