package net.rsprox.protocol.rs3.game.outgoing.model.telemetry

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public data class TelemetryGridAddColumn(
    public val group: Int,
    public val id: Int,
    public val index: Int,
) : IncomingServerGameMessage
