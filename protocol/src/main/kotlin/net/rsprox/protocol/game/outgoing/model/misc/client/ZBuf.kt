package net.rsprox.protocol.game.outgoing.model.misc.client

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

/**
 * ZBuf packet is used to toggle depth buffering in client.
 * @property enabled whether to enable depth buffering.
 */
public class ZBuf(
    public val enabled: Boolean,
) : IncomingServerGameMessage {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ZBuf

        return enabled == other.enabled
    }

    override fun hashCode(): Int {
        return enabled.hashCode()
    }

    override fun toString(): String {
        return "ZBuf(enabled=$enabled)"
    }
}
