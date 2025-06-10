package net.rsprox.protocol.game.outgoing.model.interfaces

import net.rsprot.protocol.util.CombinedId
import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

/**
 * Interface events v2 are sent to set/unlock various options on a component,
 * such as button clicks and dragging.
 * @property combinedId the bitpacked combination of [interfaceId] and [componentId].
 * @property interfaceId the interface id on which to set the events
 * @property componentId the component on that interface to set the events on
 * @property start the start subcomponent id
 * @property end the end subcomponent id (inclusive)
 * @property events1 the bitpacked events. Note that ifbutton is no longer included in this,
 * so bits 1..10 are ignored.
 * @property events2 the bitpacked ifbutton events. Each bit corresponds to the respective
 * button, including the sign bit.
 */
public class IfSetEventsV2 private constructor(
    public val combinedId: CombinedId,
    private val _start: UShort,
    private val _end: UShort,
    public val events1: Int,
    public val events2: Int,
) : IncomingServerGameMessage {
    public constructor(
        interfaceId: Int,
        componentId: Int,
        start: Int,
        end: Int,
        events1: Int,
        events2: Int,
    ) : this(
        CombinedId(interfaceId, componentId),
        start.toUShort(),
        end.toUShort(),
        events1,
        events2,
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

        other as IfSetEventsV2

        if (combinedId != other.combinedId) return false
        if (_start != other._start) return false
        if (_end != other._end) return false
        if (events1 != other.events1) return false
        if (events2 != other.events2) return false

        return true
    }

    override fun hashCode(): Int {
        var result = combinedId.hashCode()
        result = 31 * result + _start.hashCode()
        result = 31 * result + _end.hashCode()
        result = 31 * result + events1
        result = 31 * result + events2
        return result
    }

    override fun toString(): String =
        "IfSetEventsV2(" +
            "events1=$events1, " +
            "events2=$events2, " +
            "interfaceId=$interfaceId, " +
            "componentId=$componentId, " +
            "start=$start, " +
            "end=$end" +
            ")"
}
