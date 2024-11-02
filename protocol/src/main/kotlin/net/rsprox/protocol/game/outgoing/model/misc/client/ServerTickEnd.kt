package net.rsprox.protocol.game.outgoing.model.misc.client

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

/**
 * Server tick end packets are used by the C++ client
 * for ground item settings, in order to decrement
 * visible ground item's timers. Without it, all ground
 * items' timers will remain frozen once dropped.
 */
public data object ServerTickEnd : IncomingServerGameMessage
