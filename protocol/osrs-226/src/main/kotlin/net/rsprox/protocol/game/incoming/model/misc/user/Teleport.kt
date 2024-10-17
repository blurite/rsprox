package net.rsprox.protocol.game.incoming.model.misc.user

import net.rsprot.protocol.ClientProtCategory
import net.rsprox.protocol.game.incoming.model.GameClientProtCategory
import net.rsprot.protocol.message.IncomingGameMessage

/**
 * Teleport packets are sent in multiple possible scenarios:
 * 1. The player is a J-Mod and has the 'Control' and 'Shift' keys held down
 * while scrolling with their mouse wheel - the player will be teleported
 * up or down a level.
 * 2. The player is a J-Mod using an Orb of Oculus - the teleport packet
 * will be sent repeatedly every 50 client cycles (20ms/cc) while the player's
 * coordinate doesn't align up with the oculus camera center coordinate.
 * 3. The player is a J-Mod and has the 'Control' and 'Shift' keys held down
 * while clicking on the world map - the player will teleport to the coordinate
 * they clicked on in the world map.
 * @property oculusSyncValue if the player is in orb of oculus (scenario 2 above),
 * this value is equal to the last value the server transmitted with the
 * [net.rsprot.protocol.game.outgoing.prot.GameServerProt.OCULUS_SYNC] packet,
 * or 0 if the packet was never transmitted/player is not using orb of oculus.
 * @property x the absolute x coordinate to teleport to
 * @property z the absolute z coordinate to teleport to
 * @property level the height level to teleport to
 */
public class Teleport private constructor(
    public val oculusSyncValue: Int,
    private val _x: UShort,
    private val _z: UShort,
    private val _level: UByte,
) : IncomingGameMessage {
    public constructor(
        oculusSyncValue: Int,
        x: Int,
        z: Int,
        level: Int,
    ) : this(
        oculusSyncValue,
        x.toUShort(),
        z.toUShort(),
        level.toUByte(),
    )

    public val x: Int
        get() = _x.toInt()
    public val z: Int
        get() = _z.toInt()
    public val level: Int
        get() = _level.toInt()
    override val category: ClientProtCategory
        get() = GameClientProtCategory.USER_EVENT

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Teleport

        if (oculusSyncValue != other.oculusSyncValue) return false
        if (_x != other._x) return false
        if (_z != other._z) return false
        if (_level != other._level) return false

        return true
    }

    override fun hashCode(): Int {
        var result = oculusSyncValue
        result = 31 * result + _x.hashCode()
        result = 31 * result + _z.hashCode()
        result = 31 * result + _level.hashCode()
        return result
    }

    override fun toString(): String =
        "Teleport(" +
            "oculusSyncValue=$oculusSyncValue, " +
            "x=$x, " +
            "z=$z, " +
            "level=$level" +
            ")"
}
