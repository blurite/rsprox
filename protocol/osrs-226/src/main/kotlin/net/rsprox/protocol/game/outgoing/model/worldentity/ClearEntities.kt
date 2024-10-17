package net.rsprox.protocol.game.outgoing.model.worldentity

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

/**
 * Clear entities packet is used to clear any NPCs and world entities from the currently
 * active world. This furthermore sets the active world back to root.
 * This packet will not clear out any players, so the player info related to that world must
 * still be used to transfer players over.
 */
public data object ClearEntities : IncomingServerGameMessage
