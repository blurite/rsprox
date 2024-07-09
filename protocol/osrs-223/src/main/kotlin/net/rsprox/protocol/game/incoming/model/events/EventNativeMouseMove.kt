package net.rsprox.protocol.game.incoming.model.events

import net.rsprot.protocol.ClientProtCategory
import net.rsprot.protocol.message.IncomingGameMessage
import net.rsprox.protocol.game.incoming.GameClientProtCategory
import net.rsprox.protocol.game.incoming.model.events.util.MouseMovements

/**
 * Mouse move messages are sent when the user moves their mouse across
 * the client, in this case, on the enhanced C++ clients.
 * @property totalTime the total time in milliseconds that all the movements
 * inside this event span across
 * @property averageTime the average time in milliseconds between each movement.
 * The average time is truncated according to integer division rules in the JVM.
 * This is equal to `totalTime / count`.
 * @property remainingTime the remaining time from the [averageTime] integer
 * division. This is equal to `totalTime % count`.
 * @property movements all the recorded mouse movements within this message.
 * Mouse movements are recorded by the client at a 50 millisecond interval,
 * meaning any movements within that 50 milliseconds are discarded, and
 * only the position changes of the mouse at each 50 millisecond interval
 * are sent.
 */
public class EventNativeMouseMove private constructor(
    private val _averageTime: UByte,
    private val _remainingTime: UByte,
    public val movements: MouseMovements,
) : IncomingGameMessage {
    public constructor(
        averageTime: Int,
        remainingTime: Int,
        movements: MouseMovements,
    ) : this(
        averageTime.toUByte(),
        remainingTime.toUByte(),
        movements,
    )

    public val totalTime: Int
        get() = (_averageTime.toInt() * movements.length) + _remainingTime.toInt()

    public val averageTime: Int
        get() = _averageTime.toInt()

    public val remainingTime: Int
        get() = _remainingTime.toInt()
    override val category: ClientProtCategory
        get() = GameClientProtCategory.CLIENT_EVENT

    override fun toString(): String {
        return "EventNativeMouseMove(" +
            "movements=$movements, " +
            "totalTime=$totalTime, " +
            "averageTime=$averageTime, " +
            "remainingTime=$remainingTime" +
            ")"
    }
}
