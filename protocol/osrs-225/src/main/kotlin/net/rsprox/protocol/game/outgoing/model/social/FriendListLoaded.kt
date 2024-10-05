package net.rsprox.protocol.game.outgoing.model.social

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

/**
 * Friend list loaded is used to mark the friend list
 * as loaded if there are no friends to be sent.
 * If there are friends to be sent, use the [UpdateFriendList]
 * packet instead without this.
 */
public data object FriendListLoaded : IncomingServerGameMessage
