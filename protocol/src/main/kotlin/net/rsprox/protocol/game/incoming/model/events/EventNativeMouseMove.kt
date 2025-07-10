package net.rsprox.protocol.game.incoming.model.events

import net.rsprot.protocol.ClientProtCategory
import net.rsprot.protocol.message.IncomingGameMessage
import net.rsprox.protocol.game.incoming.model.GameClientProtCategory
import net.rsprox.protocol.game.incoming.model.events.util.MouseMovements

/**
 * Mouse move messages are sent when the user moves their mouse across
 * the client, in this case, on the enhanced C++ clients.
 * @property stepExcess the extra milliseconds leftover after each mouse movement
 * recording.
 * @property endExcess the extra milliseconds leftover at the end of the packet's tracking.
 * @property movements all the recorded mouse movements within this message.
 * Mouse movements are recorded by the client at a 50 millisecond interval,
 * meaning any movements within that 50 milliseconds are discarded, and
 * only the position changes of the mouse at each 50 millisecond interval
 * are sent.
 */
public class EventNativeMouseMove private constructor(
    private val _stepExcess: UByte,
    private val _endExcess: UByte,
    public val movements: MouseMovements,
) : IncomingGameMessage {
    public constructor(
        stepExcess: Int,
        endExcess: Int,
        movements: MouseMovements,
    ) : this(
        stepExcess.toUByte(),
        endExcess.toUByte(),
        movements,
    )

    public val stepExcess: Int
        get() = _stepExcess.toInt()

    public val endExcess: Int
        get() = _endExcess.toInt()
    override val category: ClientProtCategory
        get() = GameClientProtCategory.CLIENT_EVENT

    override fun toString(): String =
        "EventNativeMouseMove(" +
            "movements=$movements, " +
            "stepExcess=$stepExcess, " +
            "endExcess=$endExcess" +
            ")"
}
