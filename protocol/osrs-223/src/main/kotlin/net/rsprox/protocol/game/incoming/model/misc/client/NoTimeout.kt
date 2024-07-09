package net.rsprox.protocol.game.incoming.model.misc.client

import net.rsprot.protocol.ClientProtCategory
import net.rsprot.protocol.message.IncomingGameMessage
import net.rsprox.protocol.game.incoming.GameClientProtCategory

/**
 * No timeout packets are sent every 50 client cycles (20ms/cc)
 * to ensure the server doesn't disconnect the client due to inactivity.
 */
public data object NoTimeout : IncomingGameMessage {
    override val category: ClientProtCategory
        get() = GameClientProtCategory.CLIENT_EVENT
}
