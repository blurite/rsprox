package net.rsprox.protocol.game.outgoing.model.logout

import net.rsprot.protocol.ServerProtCategory
import net.rsprot.protocol.message.OutgoingGameMessage
import net.rsprox.protocol.game.outgoing.model.GameServerProtCategory

/**
 * Log out messages are used to tell the client the player
 * has finished playing, which then causes the client to close
 * the socket, and reset a lot of properties as a result.
 */
public data object Logout : OutgoingGameMessage {
    override val category: ServerProtCategory
        get() = GameServerProtCategory.HIGH_PRIORITY_PROT
}
