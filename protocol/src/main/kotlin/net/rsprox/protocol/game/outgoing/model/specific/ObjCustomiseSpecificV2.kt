package net.rsprox.protocol.game.outgoing.model.specific

import net.rsprox.protocol.common.CoordGrid
import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

/** Multi-index ground-item customisation introduced in revision 241. */
public data class ObjCustomiseSpecificV2(
    public val id: Int,
    public val quantity: Int,
    public val model: Int,
    public val recolours: List<Replacement>,
    public val retextures: List<Replacement>,
    public val colour: Int?,
    public val coordGrid: CoordGrid,
) : IncomingServerGameMessage {
    public data class Replacement(public val index: Int, public val value: Int)
}
