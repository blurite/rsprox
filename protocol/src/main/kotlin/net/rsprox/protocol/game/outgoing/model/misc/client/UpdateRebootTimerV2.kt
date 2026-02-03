package net.rsprox.protocol.game.outgoing.model.misc.client

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

/**
 * Update reboot timer is used to start the shut-down timer
 * in preparation of an update, along with an optional message to show.
 * @property gameCycles the number of game cycles (600ms/gc)
 * until the shut-down is complete.
 * If the number is set to zero, any existing reboot timers
 * will be cleared out.
 * The maximum possible value is 65535, which is equal to just
 * below 11 hours.
 */
public class UpdateRebootTimerV2(
    public val gameCycles: Int,
    public val messageType: UpdateMessageType,
) : IncomingServerGameMessage {
    public sealed interface UpdateMessageType

    /**
     * Sets the message to show alongside the update to the [message] provided.
     * @property message the message to show to the players, alongside the count-down.
     * Note that if the [message] is an empty string, the [IgnoreUpdateMessage] should be used instead,
     * as the client skips modifying the update message variable in this scenario.
     */
    public class SetUpdateMessage(
        public val message: String,
    ) : UpdateMessageType

    /**
     * Sets the update timer and clears any existing update message being shown to the players.
     */
    public data object ClearUpdateMessage : UpdateMessageType

    /**
     * Sets the update timer and ignores any existing update messages - if one was previously set,
     * the message would remain untouched.
     */
    public data object IgnoreUpdateMessage : UpdateMessageType

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UpdateRebootTimerV2

        if (gameCycles != other.gameCycles) return false
        if (messageType != other.messageType) return false

        return true
    }

    override fun hashCode(): Int {
        var result = gameCycles
        result = 31 * result + messageType.hashCode()
        return result
    }

    override fun toString(): String {
        return "UpdateRebootTimerV2(" +
            "gameCycles=$gameCycles, " +
            "messageType=$messageType" +
            ")"
    }
}
