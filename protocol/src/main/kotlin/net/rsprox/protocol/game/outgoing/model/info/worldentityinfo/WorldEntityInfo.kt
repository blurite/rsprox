package net.rsprox.protocol.game.outgoing.model.info.worldentityinfo

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public interface WorldEntityInfo : IncomingServerGameMessage {
    public val updates: Map<Int, WorldEntityUpdateType>
}
