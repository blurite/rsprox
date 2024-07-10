package net.rsprox.protocol.game.outgoing.model.varp

import net.rsprot.protocol.ServerProtCategory
import net.rsprot.protocol.message.OutgoingGameMessage
import net.rsprox.protocol.game.outgoing.model.GameServerProtCategory

/**
 * The varp reset packet is used to set the values of every
 * varplayer type to 0.
 * It is worth noting that the client will only reset the varps
 * up until the last one which has a respective cache config.
 * So if the varps array is extended, but respective configs
 * are not made, the extended ones will not be zero'd out.
 */
public data object VarpReset : OutgoingGameMessage {
    override val category: ServerProtCategory
        get() = GameServerProtCategory.HIGH_PRIORITY_PROT
}
