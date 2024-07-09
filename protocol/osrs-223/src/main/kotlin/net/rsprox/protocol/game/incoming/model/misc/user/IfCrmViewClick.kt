package net.rsprox.protocol.game.incoming.model.misc.user

import net.rsprot.protocol.ClientProtCategory
import net.rsprot.protocol.message.IncomingGameMessage
import net.rsprot.protocol.message.toIntOrMinusOne
import net.rsprot.protocol.util.CombinedId
import net.rsprox.protocol.game.incoming.GameClientProtCategory

/**
 * Content recommendation interface clicks happen when a player
 * clicks a component on the CRM interface, which is currently only used
 * in the form of the lobby interface, where user-specific advertisements
 * are shown.
 * Worth noting that the properties here are rough guesses at their naming
 * and the real usage has not been tested in-game.
 * @property crmServerTarget the server target, an integer
 * @property interfaceId the interface id clicked on
 * @property componentId the component id clicked on
 * @property sub the subcomponent clicked on, or -1 if none exists
 * @property behaviour1 the first CRM behaviour, an integer
 * @property behaviour2 the second CRM behaviour, an integer
 * @property behaviour3 the third CRM behaviour, an integer
 */
public class IfCrmViewClick private constructor(
    public val crmServerTarget: Int,
    private val combinedId: CombinedId,
    private val _sub: UShort,
    public val behaviour1: Int,
    public val behaviour2: Int,
    public val behaviour3: Int,
) : IncomingGameMessage {
    public constructor(
        crmServerTarget: Int,
        combinedId: CombinedId,
        sub: Int,
        behaviour1: Int,
        behaviour2: Int,
        behaviour3: Int,
    ) : this(
        crmServerTarget,
        combinedId,
        sub.toUShort(),
        behaviour1,
        behaviour2,
        behaviour3,
    )

    public val interfaceId: Int
        get() = combinedId.interfaceId
    public val componentId: Int
        get() = combinedId.componentId
    public val sub: Int
        get() = _sub.toIntOrMinusOne()
    override val category: ClientProtCategory
        get() = GameClientProtCategory.USER_EVENT

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as IfCrmViewClick

        if (crmServerTarget != other.crmServerTarget) return false
        if (combinedId != other.combinedId) return false
        if (_sub != other._sub) return false
        if (behaviour1 != other.behaviour1) return false
        if (behaviour2 != other.behaviour2) return false
        if (behaviour3 != other.behaviour3) return false

        return true
    }

    override fun hashCode(): Int {
        var result = crmServerTarget
        result = 31 * result + combinedId.hashCode()
        result = 31 * result + _sub.hashCode()
        result = 31 * result + behaviour1
        result = 31 * result + behaviour2
        result = 31 * result + behaviour3
        return result
    }

    override fun toString(): String {
        return "IfCrmViewClick(" +
            "crmServerTarget=$crmServerTarget, " +
            "interfaceId=$interfaceId, " +
            "componentId=$componentId, " +
            "sub=$sub, " +
            "behaviour1=$behaviour1, " +
            "behaviour2=$behaviour2, " +
            "behaviour3=$behaviour3" +
            ")"
    }
}
