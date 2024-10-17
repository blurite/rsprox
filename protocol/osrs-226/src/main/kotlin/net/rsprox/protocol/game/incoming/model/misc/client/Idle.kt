package net.rsprox.protocol.game.incoming.model.misc.client

import net.rsprot.protocol.ClientProtCategory
import net.rsprox.protocol.game.incoming.model.GameClientProtCategory
import net.rsprot.protocol.message.IncomingGameMessage

/**
 * Idle messages are sent if the user hasn't interacted with their
 * mouse nor their keyboard for 15,000 client cycles (20ms/cc) in a row,
 * meaning continuous inactivity for five minutes in a row.
 */
public data object Idle : IncomingGameMessage {
    override val category: ClientProtCategory
        get() = GameClientProtCategory.CLIENT_EVENT
}
