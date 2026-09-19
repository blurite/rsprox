package net.rsprox.protocol.rs3.game.outgoing.model.telemetry

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public data class TelemetryGridFull(
    public val groups: List<Group>,
) : IncomingServerGameMessage {
    public data class Group(
        public val id: Int,
        public val rowIds: List<Int>,
        public val columnIds: List<Int>,
        public val rows: List<Row>,
    )

    public data class Row(
        public val pin: Int,
        public val cells: List<Cell>,
    )

    public data class Cell(
        public val present: Int,
        public val value: Int?,
    )
}
