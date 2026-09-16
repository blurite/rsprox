package net.rsprox.protocol.rs3.game.outgoing.model.telemetry

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public data class TelemetryGridAddGroup(
    public val id: Int,
    public val index: Int,
) : IncomingServerGameMessage
