package net.rsprox.protocol.game.outgoing.model.misc.client

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

/**
 * Packet group start is a packet which tells the client to wait until the entire
 * payload of a packet group has arrived, then process all of it in a single client cycle,
 * bypassing the usual 100 packets per client cycle limitation that the client has.
 * @property length the number of bytes that will be waited for before processing everything
 * within that quantity in one client cycle.
 */
public class PacketGroupStart(
    public val length: Int,
) : IncomingServerGameMessage {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PacketGroupStart

        return length == other.length
    }

    override fun hashCode(): Int {
        return length
    }

    override fun toString(): String {
        return "PacketGroupStart(length=$length)"
    }
}
