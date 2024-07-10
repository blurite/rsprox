package net.rsprox.protocol.game.outgoing.model.camera

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

/**
 * Cam smooth reset is used to smoothly reset camera back to the
 * state where the user is in control, instead of it happening
 * instantaneously.
 *
 * Note that the properties of this packet are unused in the Java client.
 *
 * **WARNING:** The client code __requires__ that the camera is in
 * a locked state for this packet's code to be executed in **Java**.
 * If the camera isn't in a locked state, an error condition is hit
 * at the bottom of the function and the player will be kicked out of
 * the game!
 */
public class CamSmoothReset private constructor(
    private val _cameraMoveConstantSpeed: UByte,
    private val _cameraMoveProportionalSpeed: UByte,
    private val _cameraLookConstantSpeed: UByte,
    private val _cameraLookProportionalSpeed: UByte,
) : IncomingServerGameMessage {
    public constructor(
        cameraMoveConstantSpeed: Int,
        cameraMoveProportionalSpeed: Int,
        cameraLookConstantSpeed: Int,
        cameraLookProportionalSpeed: Int,
    ) : this(
        cameraMoveConstantSpeed.toUByte(),
        cameraMoveProportionalSpeed.toUByte(),
        cameraLookConstantSpeed.toUByte(),
        cameraLookProportionalSpeed.toUByte(),
    )

    public val cameraMoveConstantSpeed: Int
        get() = _cameraMoveConstantSpeed.toInt()
    public val cameraMoveProportionalSpeed: Int
        get() = _cameraMoveProportionalSpeed.toInt()
    public val cameraLookConstantSpeed: Int
        get() = _cameraLookConstantSpeed.toInt()
    public val cameraLookProportionalSpeed: Int
        get() = _cameraLookProportionalSpeed.toInt()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CamSmoothReset

        if (_cameraMoveConstantSpeed != other._cameraMoveConstantSpeed) return false
        if (_cameraMoveProportionalSpeed != other._cameraMoveProportionalSpeed) return false
        if (_cameraLookConstantSpeed != other._cameraLookConstantSpeed) return false
        if (_cameraLookProportionalSpeed != other._cameraLookProportionalSpeed) return false

        return true
    }

    override fun hashCode(): Int {
        var result = _cameraMoveConstantSpeed.hashCode()
        result = 31 * result + _cameraMoveProportionalSpeed.hashCode()
        result = 31 * result + _cameraLookConstantSpeed.hashCode()
        result = 31 * result + _cameraLookProportionalSpeed.hashCode()
        return result
    }

    override fun toString(): String {
        return "CamSmoothReset(" +
            "cameraMoveConstantSpeed=$cameraMoveConstantSpeed, " +
            "cameraMoveProportionalSpeed=$cameraMoveProportionalSpeed, " +
            "cameraLookConstantSpeed=$cameraLookConstantSpeed, " +
            "cameraLookProportionalSpeed=$cameraLookProportionalSpeed" +
            ")"
    }
}
