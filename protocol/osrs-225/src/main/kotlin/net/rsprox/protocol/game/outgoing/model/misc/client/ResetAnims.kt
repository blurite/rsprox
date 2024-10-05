package net.rsprox.protocol.game.outgoing.model.misc.client

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

/**
 * Reset anims message is used to reset the currently playing
 * animation of all NPCs and players. This does not impact
 * base animations (e.g. standing, walking).
 * It is unclear what the purpose of this packet actually is.
 */
public data object ResetAnims : IncomingServerGameMessage
