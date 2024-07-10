package net.rsprox.protocol.game.outgoing.model.varp

import net.rsprot.protocol.ServerProtCategory
import net.rsprot.protocol.message.OutgoingGameMessage
import net.rsprox.protocol.game.outgoing.model.GameServerProtCategory

/**
 * The varp sync packet is used to synchronize the client's cache
 * of varps back up with the server's version.
 *
 * The client keeps two int arrays for varps one that it modifies,
 * and one that is a perfect replica of what the server has sent.
 * This packet provides a means to sync the modified variant up
 * with what the server has sent.
 */
public data object VarpSync : OutgoingGameMessage {
    override val category: ServerProtCategory
        get() = GameServerProtCategory.HIGH_PRIORITY_PROT
}
