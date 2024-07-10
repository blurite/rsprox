package net.rsprox.protocol.game.outgoing.model.interfaces

import net.rsprot.protocol.ServerProtCategory
import net.rsprot.protocol.message.OutgoingGameMessage
import net.rsprot.protocol.util.CombinedId
import net.rsprox.protocol.game.outgoing.model.GameServerProtCategory

/**
 * If set-text packet is used to set the text on a text component.
 * @property interfaceId the interface id on which the text resides
 * @property componentId the component id on the interface on which the text
 * resides
 * @property text the text to assign
 */
public class IfSetText private constructor(
    public val combinedId: CombinedId,
    public val text: String,
) : OutgoingGameMessage {
    public constructor(
        interfaceId: Int,
        componentId: Int,
        text: String,
    ) : this(
        CombinedId(interfaceId, componentId),
        text,
    )

    public val interfaceId: Int
        get() = combinedId.interfaceId
    public val componentId: Int
        get() = combinedId.componentId
    override val category: ServerProtCategory
        get() = GameServerProtCategory.LOW_PRIORITY_PROT

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as IfSetText

        if (combinedId != other.combinedId) return false
        if (text != other.text) return false

        return true
    }

    override fun hashCode(): Int {
        var result = combinedId.hashCode()
        result = 31 * result + text.hashCode()
        return result
    }

    override fun toString(): String {
        return "IfSetText(" +
            "interfaceId=$interfaceId, " +
            "componentId=$componentId, " +
            "text='$text'" +
            ")"
    }
}
