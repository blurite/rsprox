package net.rsprox.protocol.game.outgoing.model.interfaces

import net.rsprot.protocol.util.CombinedId
import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

/**
 * If set-angle is used to change the angle of a model on an interface component.
 * @property interfaceId the interface id on which the component resides
 * @property componentId the component id on which the model resides
 * @property angleX the new x model angle to set to, a value from 0 to 2047 (inclusive)
 * @property angleY the new y model angle to set to, a value from 0 to 2047 (inclusive)
 * @property zoom the zoom of the model, defaults to a value of 100 in the client.
 * The greater the [zoom] value, the smaller the model will appear - it is inverted.
 */
public class IfSetAngle private constructor(
    public val combinedId: CombinedId,
    private val _angleX: UShort,
    private val _angleY: UShort,
    private val _zoom: UShort,
) : IncomingServerGameMessage {
    public constructor(
        interfaceId: Int,
        componentId: Int,
        angleX: Int,
        angleY: Int,
        zoom: Int,
    ) : this(
        CombinedId(interfaceId, componentId),
        angleX.toUShort(),
        angleY.toUShort(),
        zoom.toUShort(),
    )

    public val interfaceId: Int
        get() = combinedId.interfaceId
    public val componentId: Int
        get() = combinedId.componentId
    public val angleX: Int
        get() = _angleX.toInt()
    public val angleY: Int
        get() = _angleY.toInt()
    public val zoom: Int
        get() = _zoom.toInt()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as IfSetAngle

        if (combinedId != other.combinedId) return false
        if (_angleX != other._angleX) return false
        if (_angleY != other._angleY) return false
        if (_zoom != other._zoom) return false

        return true
    }

    override fun hashCode(): Int {
        var result = combinedId.hashCode()
        result = 31 * result + _angleX.hashCode()
        result = 31 * result + _angleY.hashCode()
        result = 31 * result + _zoom.hashCode()
        return result
    }

    override fun toString(): String {
        return "IfSetAngle(" +
            "interfaceId=$interfaceId, " +
            "componentId=$componentId, " +
            "angleX=$angleX, " +
            "angleY=$angleY, " +
            "zoom=$zoom" +
            ")"
    }
}
