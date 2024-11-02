package net.rsprox.protocol.world

import net.rsprox.protocol.game.outgoing.model.info.npcinfo.NpcInfoDecoder
import net.rsprox.protocol.game.outgoing.model.info.playerinfo.PlayerInfoDecoder
import net.rsprox.protocol.game.outgoing.model.info.worldentityinfo.WorldEntityInfoClient

public class World(
    public val npcInfo: NpcInfoDecoder,
    public val playerInfo: PlayerInfoDecoder,
) {
    public val worldEntityInfo: WorldEntityInfoClient = WorldEntityInfoClient()
    public var baseX: Int = -1
    public var baseZ: Int = -1
    public var level: Int = 0
}
