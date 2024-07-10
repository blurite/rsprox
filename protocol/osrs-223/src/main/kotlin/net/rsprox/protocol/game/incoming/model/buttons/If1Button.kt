package net.rsprox.protocol.game.incoming.model.buttons

import net.rsprot.protocol.ClientProtCategory
import net.rsprot.protocol.message.IncomingGameMessage
import net.rsprot.protocol.util.CombinedId
import net.rsprox.protocol.game.incoming.model.GameClientProtCategory

/**
 * If1 button messages are sent whenever a player clicks on an older
 * if1-type component.
 * @property interfaceId the interface id the player interacted with
 * @property componentId the component id on that interface the player interacted with
 */
public class If1Button(
    private val combinedId: CombinedId,
) : IncomingGameMessage {
    public val interfaceId: Int
        get() = combinedId.interfaceId
    public val componentId: Int
        get() = combinedId.componentId
    override val category: ClientProtCategory
        get() = GameClientProtCategory.USER_EVENT

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as If1Button

        return combinedId == other.combinedId
    }

    override fun hashCode(): Int {
        return combinedId.hashCode()
    }

    override fun toString(): String {
        return "If1Button(" +
            "interfaceId=$interfaceId, " +
            "componentId=$componentId" +
            ")"
    }
}
