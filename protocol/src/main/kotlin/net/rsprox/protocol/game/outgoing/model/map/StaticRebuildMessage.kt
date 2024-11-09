package net.rsprox.protocol.game.outgoing.model.map

import net.rsprot.crypto.xtea.XteaKey
import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public sealed interface StaticRebuildMessage : IncomingServerGameMessage {
    public val zoneX: Int
    public val zoneZ: Int
    public val worldArea: Int
    public val keys: List<XteaKey>
}
