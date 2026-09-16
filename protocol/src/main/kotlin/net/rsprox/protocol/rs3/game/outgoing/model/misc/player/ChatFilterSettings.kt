package net.rsprox.protocol.rs3.game.outgoing.model.misc.player

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public data class ChatFilterSettings(
    public val filterSlot1: Int,
    public val filterSlot0: Int,
) : IncomingServerGameMessage
