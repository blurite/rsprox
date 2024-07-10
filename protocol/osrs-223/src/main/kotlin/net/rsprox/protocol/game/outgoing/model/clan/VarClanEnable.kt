package net.rsprox.protocol.game.outgoing.model.clan

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

/**
 * Var clan enable packet is used to initialize a new var domain
 * in the client, intended to be sent as the player joins a clan.
 */
public data object VarClanEnable : IncomingServerGameMessage
