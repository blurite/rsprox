package net.rsprox.protocol.game.outgoing.model.interfaces

import net.rsprot.protocol.util.CombinedId
import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

/**
 * If set-npc-head-active is used to set a npc's chathead on an interface, commonly
 * in dialogues. Rather than taking the id of the npc config, this function
 * takes the index of the npc in the world. Npc's model is looked up from the
 * client through npc info, allowing for the chatbox to render a custom-built
 * npc with completely dynamic models, rather than the pre-defined configs.
 * @property interfaceId the interface id on which the model resides
 * @property componentId the component id on which the model resides
 * @property index the index of the npc in the world
 */
public class IfSetNpcHeadActive private constructor(
    public val combinedId: CombinedId,
    private val _index: UShort,
) : IncomingServerGameMessage {
    public constructor(
        interfaceId: Int,
        componentId: Int,
        index: Int,
    ) : this(
        CombinedId(interfaceId, componentId),
        index.toUShort(),
    )

    public val interfaceId: Int
        get() = combinedId.interfaceId
    public val componentId: Int
        get() = combinedId.componentId
    public val index: Int
        get() = _index.toInt()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as IfSetNpcHeadActive

        if (combinedId != other.combinedId) return false
        if (_index != other._index) return false

        return true
    }

    override fun hashCode(): Int {
        var result = combinedId.hashCode()
        result = 31 * result + _index.hashCode()
        return result
    }

    override fun toString(): String {
        return "IfSetNpcHead(" +
            "interfaceId=$interfaceId, " +
            "componentId=$componentId, " +
            "index=$index" +
            ")"
    }
}
