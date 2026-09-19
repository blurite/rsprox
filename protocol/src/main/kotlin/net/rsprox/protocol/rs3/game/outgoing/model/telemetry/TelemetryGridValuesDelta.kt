package net.rsprox.protocol.rs3.game.outgoing.model.telemetry

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public data class TelemetryGridValuesDelta(
    public val groups: List<Group>,
) : IncomingServerGameMessage {
    public data class Group(
        public val index: Int,
        public val rows: List<Row>,
    )

    public data class Row(
        public val index: Int,
        public val cells: List<Cell>,
    )

    public data class Cell(
        public val column: Int,
        public val value: Int,
    )
}
