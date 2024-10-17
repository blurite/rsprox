package net.rsprox.protocol.game.incoming.model.buttons

import net.rsprot.protocol.ClientProtCategory
import net.rsprot.protocol.message.IncomingGameMessage
import net.rsprot.protocol.message.toIntOrMinusOne
import net.rsprot.protocol.util.CombinedId
import net.rsprox.protocol.game.incoming.model.GameClientProtCategory

/**
 * Ifsubop messages are sent when the player clicks on a submenu option.
 * @property combinedId the bitpacked combination of [interfaceId] and [componentId].
 * @property interfaceId the interface id the player interacted with
 * @property componentId the component id on that interface the player interacted with
 * @property sub the subcomponent within that component if it has one, otherwise -1
 * @property obj the obj in that subcomponent, or -1
 * @property op the option clicked, ranging from 1 to 10
 * @property subop the submenu option clicked
 */
@Suppress("MemberVisibilityCanBePrivate")
public class IfSubOp private constructor(
    private val _combinedId: CombinedId,
    private val _sub: UShort,
    private val _obj: UShort,
    private val _op: UByte,
    private val _subop: UByte,
) : IncomingGameMessage {
    public constructor(
        combinedId: CombinedId,
        sub: Int,
        obj: Int,
        op: Int,
        subop: Int,
    ) : this(
        combinedId,
        sub.toUShort(),
        obj.toUShort(),
        op.toUByte(),
        subop.toUByte(),
    )

    public val combinedId: Int
        get() = _combinedId.combinedId
    public val interfaceId: Int
        get() = _combinedId.interfaceId
    public val componentId: Int
        get() = _combinedId.componentId
    public val sub: Int
        get() = _sub.toIntOrMinusOne()
    public val obj: Int
        get() = _obj.toIntOrMinusOne()
    public val op: Int
        get() = _op.toInt()
    public val subop: Int
        get() = _subop.toInt()
    override val category: ClientProtCategory
        get() = GameClientProtCategory.USER_EVENT

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as IfSubOp

        if (_combinedId != other._combinedId) return false
        if (_sub != other._sub) return false
        if (_obj != other._obj) return false
        if (_op != other._op) return false

        return true
    }

    override fun hashCode(): Int {
        var result = _combinedId.hashCode()
        result = 31 * result + _sub.hashCode()
        result = 31 * result + _obj.hashCode()
        result = 31 * result + _op.hashCode()
        return result
    }

    override fun toString(): String =
        "If3Button(" +
            "interfaceId=$interfaceId, " +
            "componentId=$componentId, " +
            "sub=$sub, " +
            "obj=$obj, " +
            "op=$op, " +
            "subop=$subop" +
            ")"
}
