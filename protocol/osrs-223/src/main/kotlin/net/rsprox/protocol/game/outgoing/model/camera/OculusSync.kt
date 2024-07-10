package net.rsprox.protocol.game.outgoing.model.camera

import net.rsprot.protocol.ServerProtCategory
import net.rsprot.protocol.message.OutgoingGameMessage
import net.rsprox.protocol.game.outgoing.model.GameServerProtCategory

/**
 * Oculus sync is used to re-synchronize the orb of oculus
 * camera to the local player in the client, if the value
 * does not match up with the client's value.
 * The client initializes this property as zero.
 * @property value the synchronization value, if the client's
 * value is different, oculus camera is moved to the client's local player.
 * Additionally, this value is sent by the client in the
 * [net.rsprot.protocol.game.incoming.misc.user.Teleport] packet whenever
 * the oculus causes the player to teleport.
 */
public class OculusSync(
    public val value: Int,
) : OutgoingGameMessage {
    override val category: ServerProtCategory
        get() = GameServerProtCategory.LOW_PRIORITY_PROT

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as OculusSync

        return value == other.value
    }

    override fun hashCode(): Int {
        return value
    }

    override fun toString(): String {
        return "OculusSync(value=$value)"
    }
}
