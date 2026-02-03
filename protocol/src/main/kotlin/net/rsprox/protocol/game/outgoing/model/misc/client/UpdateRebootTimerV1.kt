package net.rsprox.protocol.game.outgoing.model.misc.client

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

/**
 * Update reboot timer is used to start the shut-down timer
 * in preparation of an update.
 * @property gameCycles the number of game cycles (600ms/gc)
 * until the shut-down is complete.
 * If the number is set to zero, any existing reboot timers
 * will be cleared out.
 * The maximum possible value is 65535, which is equal to just
 * below 11 hours.
 */
public class UpdateRebootTimerV1(
    public val gameCycles: Int,
) : IncomingServerGameMessage {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UpdateRebootTimerV1

        return gameCycles == other.gameCycles
    }

    override fun hashCode(): Int {
        return gameCycles
    }

    override fun toString(): String {
        return "UpdateRebootTimerV1(" +
            "gameCycles=$gameCycles" +
            ")"
    }
}
