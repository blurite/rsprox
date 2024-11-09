package net.rsprox.protocol.game.outgoing.model.clan

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

/**
 * Var clan disable packet is used to clear out a var domain
 * in the client, intended to be sent as the player leaves a clan.
 */
public data object VarClanDisable : IncomingServerGameMessage
