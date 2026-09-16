package net.rsprox.protocol.rs3.game.outgoing.model.specific

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public data class NpcHeadiconSpecific(
    public val slot: Int,
    public val archive: Int,
    public val npc: Int,
    public val sprite: Int,
) : IncomingServerGameMessage
