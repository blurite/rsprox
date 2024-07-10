package net.rsprox.protocol.game.outgoing.model.interfaces

import net.rsprot.protocol.util.CombinedId
import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

/**
 * If move-sub messages are used to move a sub-level interface from
 * one position to another, typically when changing top-level interfaces.
 * @property sourceInterfaceId the current interface on which the interface that's
 * being moved is opened on
 * @property sourceComponentId the current component of the [sourceInterfaceId] on which
 * the interface that's being moved is opened on
 * @property destinationInterfaceId the destination interface on which the sub-interface
 * should be opened
 * @property destinationComponentId the component id on the [destinationInterfaceId] on
 * which the sub-interface should be opened
 */
@Suppress("MemberVisibilityCanBePrivate")
public class IfMoveSub private constructor(
    public val sourceCombinedId: CombinedId,
    public val destinationCombinedId: CombinedId,
) : IncomingServerGameMessage {
    public constructor(
        sourceInterfaceId: Int,
        sourceComponentId: Int,
        destinationInterfaceId: Int,
        destinationComponentId: Int,
    ) : this(
        CombinedId(sourceInterfaceId, sourceComponentId),
        CombinedId(destinationInterfaceId, destinationComponentId),
    )

    public val sourceInterfaceId: Int
        get() = sourceCombinedId.interfaceId
    public val sourceComponentId: Int
        get() = sourceCombinedId.componentId
    public val destinationInterfaceId: Int
        get() = destinationCombinedId.interfaceId
    public val destinationComponentId: Int
        get() = destinationCombinedId.componentId

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as IfMoveSub

        if (sourceCombinedId != other.sourceCombinedId) return false
        if (destinationCombinedId != other.destinationCombinedId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = sourceCombinedId.hashCode()
        result = 31 * result + destinationCombinedId.hashCode()
        return result
    }

    override fun toString(): String {
        return "IfMoveSub(" +
            "sourceInterfaceId=$sourceInterfaceId, " +
            "sourceComponentId=$sourceComponentId, " +
            "destinationInterfaceId=$destinationInterfaceId, " +
            "destinationComponentId=$destinationComponentId" +
            ")"
    }
}
