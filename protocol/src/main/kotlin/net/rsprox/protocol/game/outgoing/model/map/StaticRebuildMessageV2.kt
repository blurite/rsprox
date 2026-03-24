package net.rsprox.protocol.game.outgoing.model.map

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public sealed interface StaticRebuildMessageV2 : IncomingServerGameMessage {
    public val zoneX: Int
    public val zoneZ: Int
    public val worldArea: Int
}
