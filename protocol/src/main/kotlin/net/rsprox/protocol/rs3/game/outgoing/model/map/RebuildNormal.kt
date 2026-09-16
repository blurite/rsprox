package net.rsprox.protocol.rs3.game.outgoing.model.map

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage
import net.rsprox.protocol.rs3.game.outgoing.model.info.playerinfo.util.PlayerInfoInitBlock

public data class RebuildNormal(
    public val baseChunkZ: Int,
    public val format: Int,
    public val npcSceneValue: Int,
    public val reserved: Int,
    public val baseChunkX: Int,
    public val templateId: Int,
    public val minimumCoordinate: Int,
    public val maximumCoordinate: Int,
    public val sceneSize: Int,
    public val playerInfoInit: PlayerInfoInitBlock? = null,
) : IncomingServerGameMessage {
    public val baseTileX: Int
        get() = (baseChunkX - (sceneSize shr 4)) shl 3

    public val baseTileZ: Int
        get() = (baseChunkZ - (sceneSize shr 4)) shl 3
}
