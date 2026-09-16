package net.rsprox.protocol.rs3.game.outgoing.model.telemetry

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public data class TelemetryGridSetRowPinned(
    public val group: Int,
    public val row: Int,
    public val enabled: Boolean,
) : IncomingServerGameMessage
