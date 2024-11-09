package net.rsprox.protocol.game.incoming.model.misc.user

import net.rsprot.protocol.ClientProtCategory
import net.rsprot.protocol.message.IncomingGameMessage
import net.rsprox.protocol.game.incoming.model.GameClientProtCategory

/**
 * A hiscore request message is sent when a player does a lookup of another
 * player on the C++ clients. This functionality is currently not used in any way.
 * @property type the type of the request (main, ironman, group ironman etc)
 * The exact values are not yet known.
 * @property requestId the id of the request
 * @property name the name of the player whom to look up
 */
@Suppress("MemberVisibilityCanBePrivate")
public class HiscoreRequest(
    private val _type: UByte,
    private val _requestId: UByte,
    public val name: String,
) : IncomingGameMessage {
    public constructor(
        type: Int,
        requestId: Int,
        name: String,
    ) : this(
        type.toUByte(),
        requestId.toUByte(),
        name,
    )

    public val type: Int
        get() = _type.toInt()
    public val requestId: Int
        get() = _requestId.toInt()
    override val category: ClientProtCategory
        get() = GameClientProtCategory.USER_EVENT

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as HiscoreRequest

        if (_type != other._type) return false
        if (_requestId != other._requestId) return false
        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        var result = _type.hashCode()
        result = 31 * result + _requestId.hashCode()
        result = 31 * result + name.hashCode()
        return result
    }

    override fun toString(): String =
        "HiscoreRequest(" +
            "name='$name', " +
            "type=$type, " +
            "requestId=$requestId" +
            ")"
}
