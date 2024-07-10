package net.rsprox.protocol.game.outgoing.model.camera

import net.rsprot.protocol.ServerProtCategory
import net.rsprot.protocol.message.OutgoingGameMessage
import net.rsprox.protocol.game.outgoing.model.GameServerProtCategory

/**
 * Cam mode is used to set the camera into an orb-of-oculus mode,
 * or out of it.
 * @property mode the mode to set in, with the only valid values being
 * 0 for "out of oculus" and 1 for "into oculus".
 */
public class CamMode(
    public val mode: Int,
) : OutgoingGameMessage {
    override val category: ServerProtCategory
        get() = GameServerProtCategory.LOW_PRIORITY_PROT

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CamMode

        return mode == other.mode
    }

    override fun hashCode(): Int {
        return mode
    }

    override fun toString(): String {
        return "CamMode(mode=$mode)"
    }
}
