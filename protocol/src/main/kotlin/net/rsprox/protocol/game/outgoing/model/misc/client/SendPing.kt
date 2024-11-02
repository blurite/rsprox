package net.rsprox.protocol.game.outgoing.model.misc.client

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

/**
 * Send ping packet is used to request a ping response from the client.
 * The client will send these [value1] and [value2] variables back to the
 * server in exchange.
 * These integer identifiers do not appear to have any known structure to
 * them - they are not epoch time in any form. Seemingly random as the value
 * can change drastically between different logins.
 * @property value1 the first 32-bit integer identifier.
 * @property value2 the second 32-bit integer identifier.
 */
public class SendPing(
    public val value1: Int,
    public val value2: Int,
) : IncomingServerGameMessage {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SendPing

        if (value1 != other.value1) return false
        if (value2 != other.value2) return false

        return true
    }

    override fun hashCode(): Int {
        var result = value1
        result = 31 * result + value2
        return result
    }

    override fun toString(): String {
        return "SendPing(" +
            "value1=$value1, " +
            "value2=$value2" +
            ")"
    }
}
