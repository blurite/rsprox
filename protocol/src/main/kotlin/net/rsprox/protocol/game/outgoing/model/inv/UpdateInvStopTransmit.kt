package net.rsprox.protocol.game.outgoing.model.inv

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

/**
 * Update inv stop transmit is used by the server to inform the client
 * that no more updates for a given inventory are expected.
 * In OldSchool RuneScape, this is sent whenever an interface that's
 * linked to the inventory is sent.
 * In doing so, the client will wipe its cache of the given inventory.
 * There is no technical reason to send this, however, as it doesn't
 * prevent anything from functioning as normal.
 * @property inventoryId the id of the inventory to stop listening to
 */
public class UpdateInvStopTransmit(
    public val inventoryId: Int,
) : IncomingServerGameMessage {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UpdateInvStopTransmit

        return inventoryId == other.inventoryId
    }

    override fun hashCode(): Int {
        return inventoryId
    }

    override fun toString(): String {
        return "UpdateInvStopTransmit(inventoryId=$inventoryId)"
    }
}
