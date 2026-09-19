package net.rsprox.protocol.rs3.game.outgoing.model.telemetry

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public data class TelemetryGridRemoveColumn(
    public val column: Int,
    public val group: Int,
) : IncomingServerGameMessage
