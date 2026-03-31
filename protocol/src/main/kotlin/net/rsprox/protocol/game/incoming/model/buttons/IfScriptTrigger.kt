package net.rsprox.protocol.game.incoming.model.buttons

import net.rsprot.protocol.ClientProtCategory
import net.rsprot.protocol.message.IncomingGameMessage
import net.rsprot.protocol.message.toIntOrMinusOne
import net.rsprot.protocol.util.CombinedId
import net.rsprox.protocol.game.incoming.model.GameClientProtCategory

/**
 * IfScriptTrigger is used by the client to run a server script that is associated with the component.
 * @property combinedId the bitpacked combination of [interfaceId] and [componentId].
 * @property interfaceId the interface id the player interacted with
 * @property componentId the component id on that interface the script is associated to
 * @property sub the subcomponent within that component if it has one, otherwise -1
 * @property obj the obj in that subcomponent, or -1
 * @property crc the crc of the script serverside (not confirmed*)
 * @property bytes the backing bytebuf for the arguments. Since we lack information about them,
 * we cannot decode it.
 */
@Suppress("MemberVisibilityCanBePrivate")
public class IfScriptTrigger private constructor(
    private val _combinedId: CombinedId,
    private val _sub: UShort,
    private val _obj: UShort,
    public val crc: Int,
    public val bytes: ByteArray,
) : IncomingGameMessage {
    public constructor(
        combinedId: CombinedId,
        sub: Int,
        obj: Int,
        crc: Int,
        bytes: ByteArray,
    ) : this(
        combinedId,
        sub.toUShort(),
        obj.toUShort(),
        crc,
        bytes,
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

    override val category: ClientProtCategory
        get() = GameClientProtCategory.USER_EVENT

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as IfScriptTrigger

        if (_combinedId != other._combinedId) return false
        if (_sub != other._sub) return false
        if (_obj != other._obj) return false
        if (crc != other.crc) return false
        if (bytes != other.bytes) return false

        return true
    }

    override fun hashCode(): Int {
        var result = _combinedId.hashCode()
        result = 31 * result + _sub.hashCode()
        result = 31 * result + _obj.hashCode()
        result = 31 * result + crc.hashCode()
        result = 31 * result + bytes.hashCode()
        return result
    }

    override fun toString(): String =
        "IfScriptTrigger(" +
            "interfaceId=$interfaceId, " +
            "componentId=$componentId, " +
            "sub=$sub, " +
            "obj=$obj, " +
            "crc=$crc" +
            ")"
}
