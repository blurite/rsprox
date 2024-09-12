package net.rsprox.protocol.world

import net.rsprox.cache.api.CacheProvider
import net.rsprox.protocol.game.outgoing.model.info.npcinfo.NpcInfoClient
import net.rsprox.protocol.game.outgoing.model.info.worldentityinfo.WorldEntityInfoClient

public class World(
    cache: CacheProvider,
) {
    public val npcInfo: NpcInfoClient = NpcInfoClient(cache)
    public val worldEntityInfo: WorldEntityInfoClient = WorldEntityInfoClient()
    public var baseX: Int = -1
    public var baseZ: Int = -1
    public var level: Int = 0
}
