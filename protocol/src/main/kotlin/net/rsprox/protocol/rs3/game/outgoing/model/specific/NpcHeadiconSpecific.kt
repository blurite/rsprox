package net.rsprox.protocol.rs3.game.outgoing.model.specific

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public data class NpcHeadiconSpecific(
    public val slot: Int,
    public val id: Int,
    public val npcIndex: Int,
    public val spriteIndex: Int,
) : IncomingServerGameMessage
