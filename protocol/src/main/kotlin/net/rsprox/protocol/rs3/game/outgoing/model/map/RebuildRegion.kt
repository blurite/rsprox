package net.rsprox.protocol.rs3.game.outgoing.model.map

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage
import net.rsprox.protocol.rs3.game.outgoing.model.info.playerinfo.util.PlayerInfoInitBlock

public data class RebuildRegion(
    public val baseChunkZ: Int,
    public val unused: Int,
    /** Bit width of each signed, local-player-relative NPC-add X/Z offset. */
    public val npcCoordinateBits: Int,
    public val format: Int,
    public val mode: Int,
    public val baseChunkX: Int,
    public val regionOriginX: Int,
    public val regionOriginZ: Int,
    public val rows: Int,
    public val columns: Int,
    public val templates: List<List<List<Int>>>,
    public val sceneSize: Int,
    public val playerInfoInit: PlayerInfoInitBlock? = null,
) : IncomingServerGameMessage {
    public val baseTileX: Int
        get() = (baseChunkX - (sceneSize shr 4)) shl 3

    public val baseTileZ: Int
        get() = (baseChunkZ - (sceneSize shr 4)) shl 3
}
