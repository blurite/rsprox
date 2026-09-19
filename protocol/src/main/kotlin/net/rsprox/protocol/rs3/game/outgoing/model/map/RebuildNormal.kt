package net.rsprox.protocol.rs3.game.outgoing.model.map

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage
import net.rsprox.protocol.rs3.game.outgoing.model.info.playerinfo.util.PlayerInfoInitBlock

public data class RebuildNormal(
    public val baseChunkZ: Int,
    public val format: Int,
    /** Bit width of each signed, local-player-relative NPC-add X/Z offset. */
    public val npcCoordinateBits: Int,
    /** Raw bytes skipped by the native handler, retained only for inspection. */
    public val unused: Int,
    public val baseChunkX: Int,
    /** World-area definition in config group 83. */
    public val worldAreaId: Int,
    /** Packed coordinate; native scene loading uses its X/Z map-square bounds. */
    public val minimumCoordinate: Int,
    /** Packed coordinate; the containing map square is included in the loading bounds. */
    public val maximumCoordinate: Int,
    public val sceneSize: Int,
    public val playerInfoInit: PlayerInfoInitBlock? = null,
) : IncomingServerGameMessage {
    public val baseTileX: Int
        get() = (baseChunkX - (sceneSize shr 4)) shl 3

    public val baseTileZ: Int
        get() = (baseChunkZ - (sceneSize shr 4)) shl 3
}
