package net.rsprox.protocol.game.incoming.model.misc.client

import net.rsprot.protocol.ClientProtCategory
import net.rsprox.protocol.game.incoming.model.GameClientProtCategory
import net.rsprot.protocol.message.IncomingGameMessage

/**
 * Sends a ping reply to the server whenever the server requests it.
 * @property fps the current fps of the client at the time of the message
 * @property gcPercentTime the approximate percentage of the time spent
 * garbage collecting
 * @property value1 the first integer value sent by the serer
 * @property value2 the second integer value sent by the server
 */
@Suppress("MemberVisibilityCanBePrivate")
public class SendPingReply private constructor(
    private val _fps: UByte,
    private val _gcPercentTime: UByte,
    public val value1: Int,
    public val value2: Int,
) : IncomingGameMessage {
    public constructor(
        fps: Int,
        gcPercentTime: Int,
        value1: Int,
        value2: Int,
    ) : this(
        fps.toUByte(),
        gcPercentTime.toUByte(),
        value1,
        value2,
    )

    public val fps: Int
        get() = _fps.toInt()
    public val gcPercentTime: Int
        get() = _gcPercentTime.toInt()
    override val category: ClientProtCategory
        get() = GameClientProtCategory.USER_EVENT

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SendPingReply

        if (_fps != other._fps) return false
        if (_gcPercentTime != other._gcPercentTime) return false
        if (value1 != other.value1) return false
        if (value2 != other.value2) return false

        return true
    }

    override fun hashCode(): Int {
        var result = _fps.hashCode()
        result = 31 * result + _gcPercentTime.hashCode()
        result = 31 * result + value1
        result = 31 * result + value2
        return result
    }

    override fun toString(): String =
        "SendPingReply(" +
            "fps=$fps, " +
            "gcPercentTime=$gcPercentTime, " +
            "value1=$value1, " +
            "value2=$value2" +
            ")"
}
