package net.rsprox.protocol.rs3.game.outgoing.model.social

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public data class UpdateIgnorelist(
    public val ignores: List<Ignore>,
) : IncomingServerGameMessage {
    public data class Ignore(
        public val flags: Int,
        public val name: String,
        public val previousName: String,
        public val note: String,
    )
}
