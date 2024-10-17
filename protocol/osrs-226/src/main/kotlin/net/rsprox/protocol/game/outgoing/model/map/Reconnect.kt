package net.rsprox.protocol.game.outgoing.model.map

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage
import net.rsprox.protocol.game.outgoing.model.info.playerinfo.util.PlayerInfoInitBlock

/**
 * Reconnect is sent when the client goes through a reconnection.
 * @property playerInfoInitBlock the initialization block of player info, containing the
 * absolute coordinate of the local player, and low resolution positions of everyone else.
 */
public class Reconnect(
    public val playerInfoInitBlock: PlayerInfoInitBlock,
) : IncomingServerGameMessage {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Reconnect

        return playerInfoInitBlock == other.playerInfoInitBlock
    }

    override fun hashCode(): Int {
        return playerInfoInitBlock.hashCode()
    }

    override fun toString(): String {
        return "Reconnect(playerInfoInitBlock=$playerInfoInitBlock)"
    }
}
