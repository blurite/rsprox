package net.rsprox.protocol.rs3.game.outgoing.model.specific

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage
import net.rsprox.protocol.rs3.game.outgoing.model.zone.payload.ProjectileOffset

/**
 * Same effect and target semantics as SpotanimSpecific, with an additional positional offset.
 * Height is signed native fine units, and id -1 denotes removal.
 */
public data class SpotanimSpecificV2(
    public val target: Int,
    public val rotationFlags: Int,
    public val offset: ProjectileOffset,
    public val slot: Int,
    public val height: Int,
    public val id: Int,
    public val packedDelay: Int,
) : IncomingServerGameMessage {
    public val effect: SpotanimSpecific
        get() = SpotanimSpecific(height, packedDelay, rotationFlags, target, id, slot)
}
