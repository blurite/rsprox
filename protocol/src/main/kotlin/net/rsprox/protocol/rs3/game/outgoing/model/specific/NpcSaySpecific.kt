package net.rsprox.protocol.rs3.game.outgoing.model.specific

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public data class NpcSaySpecific(
    public val text: String,
    public val colour: Int,
    public val npcIndex: Int,
    public val effect: Int,
) : IncomingServerGameMessage
