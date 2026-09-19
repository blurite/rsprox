package net.rsprox.protocol.rs3.game.outgoing.model.interfaces

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public data class IfOpenSubActiveNpc(
    public val legacyWord0: Int,
    public val npcIndex: Int,
    public val childId: Int,
    public val legacyWord1: Int,
    public val componentHash: Long,
    public val legacyWord2: Int,
    public val legacyWord3: Int,
    public val layer: Int,
) : IncomingServerGameMessage
