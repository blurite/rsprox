package net.rsprox.protocol.rs3.game.outgoing.model.misc.client

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public data class DebugServerTriggers(
    public val interfaceId: Int,
    public val start: Int,
    public val endExclusive: Int,
    public val records: List<Record>,
) : IncomingServerGameMessage {
    public data class Record(
        public val unused0: Int,
        public val unused1: Int,
        public val unused2: Int,
    )
}
