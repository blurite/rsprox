package net.rsprox.protocol.game.outgoing.model.misc.client

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

/**
 * A data object to mark the end of a packet group
 */
public data class PacketGroupEnd(
    public val bytesRead: Int,
) : IncomingServerGameMessage
