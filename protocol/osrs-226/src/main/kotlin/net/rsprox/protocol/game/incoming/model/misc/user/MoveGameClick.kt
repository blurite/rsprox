package net.rsprox.protocol.game.incoming.model.misc.user

import net.rsprot.protocol.ClientProtCategory
import net.rsprot.protocol.message.IncomingGameMessage
import net.rsprox.protocol.game.incoming.model.GameClientProtCategory
import net.rsprox.protocol.game.incoming.model.misc.user.internal.MovementRequest

/**
 * Move gameclick packets are sent when the user clicks to walk within their
 * main game window (not minimap).
 * @property x the absolute x coordinate to walk to
 * @property z the absolute z coordinate to walk to
 * @property keyCombination the combination of keys held down to move there.
 * Possible values include 0, 1 and 2, where:
 * A value of 2 is sent if the user is holding down the 'Control' and 'Shift' keys
 * simultaneously.
 * A value of 1 is sent if the user is holding down the 'Control' key without
 * the 'Shift' key.
 * In any other scenario, a value of 0 is sent.
 * The 'Control' key is used to invert move speed for the single movement request,
 * and the 'Control' + 'Shift' combination is presumably for J-Mods to teleport
 * around - although there are no validations for J-Mod privileges in the client,
 * it will send the value of 2 even for regular users.
 */
@Suppress("MemberVisibilityCanBePrivate")
public class MoveGameClick private constructor(
    private val movementRequest: MovementRequest,
) : IncomingGameMessage {
    public constructor(
        x: Int,
        z: Int,
        keyCombination: Int,
    ) : this(
        MovementRequest(
            x,
            z,
            keyCombination,
        ),
    )

    public val x: Int
        get() = movementRequest.x
    public val z: Int
        get() = movementRequest.z
    public val keyCombination: Int
        get() = movementRequest.keyCombination
    override val category: ClientProtCategory
        get() = GameClientProtCategory.USER_EVENT

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MoveGameClick

        return movementRequest == other.movementRequest
    }

    override fun hashCode(): Int = movementRequest.hashCode()

    override fun toString(): String =
        "MoveGameClick(" +
            "x=$x, " +
            "z=$z, " +
            "keyCombination=$keyCombination" +
            ")"
}
