package net.rsprox.protocol.rs3.game.outgoing.model.misc.client

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public data class Unnamed1(
    public val mode: Int,
    public val modeWord: Int?,
    public val modeBytes: ModeBytes?,
    public val textFlag: Int,
    public val discardedText: String?,
) : IncomingServerGameMessage {
    /** These three bytes are skipped by native code; their meaning and numeric encoding are unknown. */
    public data class ModeBytes(
        public val unused0: Int,
        public val unused1: Int,
        public val unused2: Int,
    )
}
