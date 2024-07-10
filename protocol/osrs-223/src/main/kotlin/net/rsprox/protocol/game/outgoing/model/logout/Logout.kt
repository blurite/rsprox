package net.rsprox.protocol.game.outgoing.model.logout

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

/**
 * Log out messages are used to tell the client the player
 * has finished playing, which then causes the client to close
 * the socket, and reset a lot of properties as a result.
 */
public data object Logout : IncomingServerGameMessage
