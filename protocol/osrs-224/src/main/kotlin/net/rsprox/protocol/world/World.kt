package net.rsprox.protocol.world

import net.rsprot.compression.HuffmanCodec
import net.rsprox.cache.api.CacheProvider
import net.rsprox.protocol.game.outgoing.model.info.npcinfo.NpcInfoClient
import net.rsprox.protocol.game.outgoing.model.info.playerinfo.PlayerInfoClient
import net.rsprox.protocol.game.outgoing.model.info.worldentityinfo.WorldEntityInfoClient

public class World(
    localPlayerIndex: Int,
    huffmanCodec: HuffmanCodec,
    cache: CacheProvider,
) {
    public val playerInfo: PlayerInfoClient = PlayerInfoClient(localPlayerIndex, huffmanCodec)
    public val npcInfo: NpcInfoClient = NpcInfoClient(cache)
    public val worldEntityInfo: WorldEntityInfoClient = WorldEntityInfoClient()
    public var baseX: Int = -1
    public var baseZ: Int = -1
    public var level: Int = 0
}
