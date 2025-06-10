package net.rsprox.protocol.game.outgoing.model.interfaces

import net.rsprot.protocol.util.CombinedId
import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

/**
 * Interface events are sent to set/unlock various options on a component,
 * such as button clicks and dragging.
 * @property interfaceId the interface id on which to set the events
 * @property componentId the component on that interface to set the events on
 * @property start the start subcomponent id
 * @property end the end subcomponent id (inclusive)
 * @property events the bitpacked events
 */
public class IfSetEventsV1 private constructor(
    public val combinedId: CombinedId,
    private val _start: UShort,
    private val _end: UShort,
    public val events: Int,
) : IncomingServerGameMessage {
    public constructor(
        interfaceId: Int,
        componentId: Int,
        start: Int,
        end: Int,
        events: Int,
    ) : this(
        CombinedId(interfaceId, componentId),
        start.toUShort(),
        end.toUShort(),
        events,
    )

    public val interfaceId: Int
        get() = combinedId.interfaceId
    public val componentId: Int
        get() = combinedId.componentId
    public val start: Int
        get() = _start.toInt()
    public val end: Int
        get() = _end.toInt()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as IfSetEventsV1

        if (combinedId != other.combinedId) return false
        if (_start != other._start) return false
        if (_end != other._end) return false
        if (events != other.events) return false

        return true
    }

    override fun hashCode(): Int {
        var result = combinedId.hashCode()
        result = 31 * result + _start.hashCode()
        result = 31 * result + _end.hashCode()
        result = 31 * result + events
        return result
    }

    override fun toString(): String {
        return "IfSetEventsV1(" +
            "events=$events, " +
            "interfaceId=$interfaceId, " +
            "componentId=$componentId, " +
            "start=$start, " +
            "end=$end" +
            ")"
    }
}
