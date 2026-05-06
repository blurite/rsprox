package net.rsprox.protocol.game.outgoing.model.camera

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

/**
 * Unlocks the camera's min and max pitch angle, allowing one to pan the camera fully vertical,
 * or into the ground.
 */
public class CamUnlock(
    public val unlock: Boolean,
) : IncomingServerGameMessage {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CamUnlock

        return unlock == other.unlock
    }

    override fun hashCode(): Int {
        return unlock.hashCode()
    }

    override fun toString(): String =
        "CamUnlock(" +
            "unlock=$unlock" +
            ")"
}
