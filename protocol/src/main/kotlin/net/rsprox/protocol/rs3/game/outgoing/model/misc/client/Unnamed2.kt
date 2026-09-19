package net.rsprox.protocol.rs3.game.outgoing.model.misc.client

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public data class Unnamed2(
    public val count: Int,
    public val records: List<Record>,
    public val discardedFooter: Int,
) : IncomingServerGameMessage {
    public data class Record(
        public val unused0: Int,
        public val unused1: Int,
        public val unused2: Int,
        public val unused3: Int,
    )
}
