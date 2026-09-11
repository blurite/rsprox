package net.rsprox.protocol.rs3.game.outgoing.model.map

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage
import net.rsprox.protocol.rs3.game.outgoing.model.info.playerinfo.util.PlayerInfoInitBlock

public class RebuildNormal(
    public val playerInfoInitBlock: PlayerInfoInitBlock?,
    public val playerCoordX: Int,
    public val playerCoordY: Int,
    public val coordBitWidth: Int,
    public val trailerAligned: Boolean,
    public val worldAreaTypeId: Int,
    public val worldSouthWest: Int,
    public val worldNorthEast: Int,
    public val baseTileX: Int,
    public val baseTileZ: Int,
) : IncomingServerGameMessage {
    override fun toString(): String {
        return "RebuildNormal(" +
            "playerInfoInitBlock=$playerInfoInitBlock, " +
            "trailerAligned=$trailerAligned, " +
            "baseTileX=$baseTileX, baseTileZ=$baseTileZ" +
            ")"
    }
}
