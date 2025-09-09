package net.rsprox.protocol.game.outgoing.model.misc.player

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

/**
 * Account flags are used to set certain features in the client for given players.
 *
 * Below is a table of known flags:
 *
 * ```
 * | Bit | Feature                                |
 * |-----|----------------------------------------|
 * | 35  | Enable Lua Plugin Development Commands |
 * ```
 */
public class AccountFlags(
    public val flags: Long,
) : IncomingServerGameMessage {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AccountFlags

        return flags == other.flags
    }

    override fun hashCode(): Int {
        return flags.hashCode()
    }

    override fun toString(): String = "AccountFlags(flags=$flags)"
}
