package net.rsprox.protocol.game.outgoing.model.camera

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

/**
 * Sets the skybox model to render.
 */
public class CamSkybox(
    public val model: Int,
) : IncomingServerGameMessage {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CamSkybox

        return model == other.model
    }

    override fun hashCode(): Int {
        return model.hashCode()
    }

    override fun toString(): String =
        "CamSkybox(" +
            "model=$model" +
            ")"
}
